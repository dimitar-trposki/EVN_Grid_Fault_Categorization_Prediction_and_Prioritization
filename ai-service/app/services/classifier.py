from __future__ import annotations

from collections import Counter
from pathlib import Path
import logging
from typing import Any

import joblib

from app.schemas import ClassificationRequest, ClassificationResponse

logger = logging.getLogger(__name__)
ROOT = Path(__file__).resolve().parents[2]
CLASSIFIER_MODEL_PATH = ROOT / "models" / "classifier.joblib"
SEVERITY_MODEL_PATH = ROOT / "models" / "severity.joblib"

classifier_model: Any | None = None
severity_model: Any | None = None
_models_loaded = False

try:
    import spacy

    try:
        spacy_nlp = spacy.load("en_core_web_sm")
        logger.info("spaCy model en_core_web_sm loaded")
    except Exception as exc:
        spacy_nlp = None
        logger.warning("spaCy model unavailable; keyword extraction will use fallback: %s", exc)
except Exception as exc:  # pragma: no cover
    spacy_nlp = None
    logger.warning("spaCy unavailable; keyword extraction will use fallback: %s", exc)

CATEGORY_KEYWORDS = {
    "ELECTRICAL": ["транс", "трафо", "струја", "светл", "voltage", "electric", "power", "transform"],
    "MECHANICAL": ["дрво", "столб", "бандер", "паднат", "скршен", "tree", "pole", "fallen", "broken"],
    "NETWORK": ["сензор", "signal", "telemetry", "scada", "comm"],
    "SOFTWARE": ["dashboard", "app", "system", "login", "application", "грешка во систем"],
}
SAFETY_RISK_KEYWORDS = [
    "пожар", "fire", "експлозија", "explosion", "искр", "spark", "чад", "smoke",
    "гола жица", "exposed wire", "downed line", "паднат вод", "electrocut",
]
STOPWORDS = {
    "и", "во", "на", "за", "со", "од", "до", "по", "кај", "без", "дека", "ова", "тоа",
    "the", "and", "is", "of", "to", "in", "on", "for", "a", "an", "with", "by", "or", "at", "from",
}


def _load_models() -> tuple[Any | None, Any | None]:
    global classifier_model, severity_model, _models_loaded
    if _models_loaded:
        return classifier_model, severity_model

    try:
        if CLASSIFIER_MODEL_PATH.exists():
            classifier_model = joblib.load(CLASSIFIER_MODEL_PATH)
            logger.info("Loaded classifier model from %s", CLASSIFIER_MODEL_PATH)
        else:
            logger.warning("Classifier model missing at %s", CLASSIFIER_MODEL_PATH)
    except Exception as exc:
        classifier_model = None
        logger.exception("Failed to load classifier model: %s", exc)

    try:
        if SEVERITY_MODEL_PATH.exists():
            severity_model = joblib.load(SEVERITY_MODEL_PATH)
            logger.info("Loaded severity model from %s", SEVERITY_MODEL_PATH)
        else:
            logger.warning("Severity model missing at %s", SEVERITY_MODEL_PATH)
    except Exception as exc:
        severity_model = None
        logger.exception("Failed to load severity model: %s", exc)

    _models_loaded = True
    logger.info("Model load status: classifier=%s severity=%s", classifier_model is not None,
                severity_model is not None)
    return classifier_model, severity_model


def _rule_category(text_lower: str) -> str:
    for category, keywords in CATEGORY_KEYWORDS.items():
        if any(keyword in text_lower for keyword in keywords):
            return category
    return "OTHER"


def _rule_severity(text_lower: str) -> str:
    if any(keyword in text_lower for keyword in ["пожар", "fire", "експлозија", "explosion"]):
        return "CRITICAL"
    if any(keyword in text_lower for keyword in ["искр", "spark", "чад", "smoke"]):
        return "HIGH"
    return "MEDIUM"


def _safety_risk(text_lower: str) -> bool:
    return any(keyword in text_lower for keyword in SAFETY_RISK_KEYWORDS)


def _keywords(text: str) -> list[str]:
    try:
        if spacy_nlp is not None:
            doc = spacy_nlp(text)
            seen: set[str] = set()
            terms: list[str] = []
            for chunk in doc.noun_chunks:
                value = chunk.text.strip().lower()
                if len(value) > 2 and value not in seen:
                    seen.add(value)
                    terms.append(value)
                if len(terms) >= 5:
                    return terms
            for ent in doc.ents:
                value = ent.text.strip().lower()
                if len(value) > 2 and value not in seen:
                    seen.add(value)
                    terms.append(value)
                if len(terms) >= 5:
                    return terms
            if terms:
                return terms[:5]
    except Exception as exc:
        logger.warning("spaCy keyword extraction failed; falling back: %s", exc)

    cleaned = "".join(ch.lower() if ch.isalnum() or ch.isspace() else " " for ch in text)
    words = [word for word in cleaned.split() if len(word) > 3 and word not in STOPWORDS]
    return [word for word, _ in Counter(words).most_common(5)]


def classify(request: ClassificationRequest) -> ClassificationResponse:
    text = request.description.strip()
    if request.location_text:
        text = f"{text} | {request.location_text.strip()}"
    text_lower = text.lower()

    try:
        clf, sev = _load_models()
        if clf is not None and sev is not None:
            predicted_category = str(clf.predict([text])[0])
            probabilities = clf.predict_proba([text])[0]
            confidence = float(max(probabilities))
            predicted_severity = str(sev.predict([text])[0])
            is_fallback = False
        else:
            predicted_category = _rule_category(text_lower)
            predicted_severity = _rule_severity(text_lower)
            confidence = 0.6
            is_fallback = True

        # Safety words should dominate severity even when a synthetic model is uncertain.
        if any(keyword in text_lower for keyword in ["пожар", "fire", "експлозија", "explosion"]):
            predicted_severity = "CRITICAL"
        elif any(keyword in text_lower for keyword in ["искр", "spark", "чад", "smoke"]):
            predicted_severity = "HIGH"

        return ClassificationResponse(
            fault_id=request.fault_id,
            predicted_category=predicted_category,
            predicted_severity=predicted_severity,
            confidence=confidence,
            keywords=_keywords(text),
            safety_risk=_safety_risk(text_lower),
            is_fallback=is_fallback,
        )
    except Exception as exc:  # pragma: no cover
        logger.exception("Classification failed: %s", exc)
        return ClassificationResponse(
            fault_id=request.fault_id,
            predicted_category=_rule_category(text_lower),
            predicted_severity=_rule_severity(text_lower),
            confidence=0.6,
            keywords=_keywords(text),
            safety_risk=_safety_risk(text_lower),
            is_fallback=True,
        )

# def classify(request):
#     raise NotImplementedError("Classifier not yet implemented")
#
#
# def classify_fault(description: str):
#     desc = description.lower()
#
#     keywords = []
#     if "transformer" in desc:
#         keywords.append("transformer")
#     if "burn" in desc or "overheat" in desc:
#         keywords.append("overheating")
#
#     severity = "LOW"
#     if "fire" in desc or "burn" in desc:
#         severity = "CRITICAL"
#
#     return {
#         "predictedCategory": "ELECTRICAL_FAILURE",
#         "predictedSeverity": severity,
#         "confidence": 0.85,
#         "keywords": keywords,
#         "safetyRisk": severity in ["HIGH", "CRITICAL"]
#     }
