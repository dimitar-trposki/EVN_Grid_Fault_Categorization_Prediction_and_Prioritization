from __future__ import annotations

import logging
from collections import Counter
from pathlib import Path
from typing import List

import joblib

from app.schemas import ClassificationRequest, ClassificationResponse

logger = logging.getLogger(__name__)

# ── Model paths ──────────────────────────────────────────────────
_MODELS_DIR = Path(__file__).resolve().parent.parent.parent / "models"
_CLASSIFIER_PATH = _MODELS_DIR / "classifier.joblib"
_SEVERITY_PATH = _MODELS_DIR / "severity.joblib"

# ── Lazy-loaded model caches ─────────────────────────────────────
_classifier_model = None
_severity_model = None
_models_loaded = False

# ── Optional spaCy ───────────────────────────────────────────────
try:
    import spacy
    spacy_nlp = spacy.load("en_core_web_sm")
    logger.info("spaCy en_core_web_sm loaded successfully")
except Exception:
    spacy_nlp = None
    logger.info("spaCy not available — will use keyword-based extraction")

# ── Stopwords for fallback keyword extraction ────────────────────
_STOPWORDS = frozenset({
    # Macedonian
    "и", "во", "на", "за", "од", "до", "со", "по", "се", "не", "да", "е",
    "ке", "ќе", "ги", "го", "ја", "ни", "ми", "си", "му", "им", "нас",
    "вас", "она", "тоа", "оваа", "овој", "ова", "тие", "таа", "тој",
    "што", "кај", "дека", "или", "ама", "ние", "вие", "нема", "има",
    "бил", "била", "биле", "било", "сме", "сте", "при", "веќе", "уште",
    "само", "еден", "една", "едно", "може", "треба",
    # English
    "the", "and", "is", "of", "to", "in", "a", "an", "it", "for",
    "on", "at", "by", "or", "not", "but", "with", "from", "are",
    "was", "were", "has", "had", "have", "this", "that", "can",
    "will", "been", "just", "all", "very", "near", "after", "some",
})

# ── Safety keywords ──────────────────────────────────────────────
_SAFETY_KEYWORDS = [
    "пожар", "fire", "експлозија", "explosion",
    "искр", "spark", "чад", "smoke",
    "гола жица", "exposed wire", "downed line",
    "паднат вод", "electrocut",
    "жива жица", "live wire", "downed wire",
    "пламен", "гори",
]


def _load_models() -> None:
    """Lazy-load classifier and severity models from disk."""
    global _classifier_model, _severity_model, _models_loaded
    if _models_loaded:
        return
    _models_loaded = True

    try:
        _classifier_model = joblib.load(_CLASSIFIER_PATH)
        logger.info("Classifier model loaded from %s", _CLASSIFIER_PATH)
    except Exception:
        logger.warning("Could not load classifier model from %s", _CLASSIFIER_PATH)
        _classifier_model = None

    try:
        _severity_model = joblib.load(_SEVERITY_PATH)
        logger.info("Severity model loaded from %s", _SEVERITY_PATH)
    except Exception:
        logger.warning("Could not load severity model from %s", _SEVERITY_PATH)
        _severity_model = None


def _detect_safety_risk(text: str) -> bool:
    """Rule-based safety risk detection — always applied."""
    low = text.lower()
    return any(kw in low for kw in _SAFETY_KEYWORDS)


def _rule_based_category(text: str) -> str:
    low = text.lower()
    electrical_kw = ["транс", "трафо", "струја", "светл", "voltage", "electric",
                     "power", "transform", "напон", "прекин", "outage"]
    mechanical_kw = ["дрво", "столб", "бандер", "паднат", "скршен", "tree",
                     "pole", "fallen", "broken", "жица", "wire", "гранк"]
    network_kw = ["сензор", "signal", "telemetry", "scada", "comm", "rtu",
                  "бројач", "meter", "sensor"]
    software_kw = ["dashboard", "app", "system", "login", "application",
                   "грешка во систем", "софтвер", "портал", "апликација",
                   "модул", "копче", "button"]

    if any(kw in low for kw in network_kw):
        return "NETWORK"
    if any(kw in low for kw in software_kw):
        return "SOFTWARE"
    if any(kw in low for kw in mechanical_kw):
        return "MECHANICAL"
    if any(kw in low for kw in electrical_kw):
        return "ELECTRICAL"
    return "OTHER"


def _rule_based_severity(text: str) -> str:
    low = text.lower()
    if any(kw in low for kw in ["пожар", "fire", "експлозија", "explosion"]):
        return "CRITICAL"
    if any(kw in low for kw in ["искр", "spark", "чад", "smoke"]):
        return "HIGH"
    return "MEDIUM"


def _extract_keywords(text: str) -> List[str]:
    """Extract keywords using spaCy noun chunks or fallback word frequency."""
    if spacy_nlp is not None:
        doc = spacy_nlp(text)
        chunks = []
        seen = set()
        for chunk in doc.noun_chunks:
            t = chunk.text.strip()
            if len(t) > 2 and t.lower() not in seen:
                seen.add(t.lower())
                chunks.append(t)
        return chunks[:5]

    # Fallback: word frequency
    words = text.split()
    filtered = [w.lower() for w in words if len(w) > 3 and w.lower() not in _STOPWORDS]
    freq = Counter(filtered)
    return [w for w, _ in freq.most_common(5)]


def classify(request: ClassificationRequest) -> ClassificationResponse:
    """Classify a fault report by category and severity."""
    _load_models()

    # Build text input
    text = request.description
    if request.locationText:
        text = text + " | " + request.locationText

    # Prediction
    if _classifier_model is not None and _severity_model is not None:
        predicted_category = _classifier_model.predict([text])[0]
        confidence = float(max(_classifier_model.predict_proba([text])[0]))
        predicted_severity = _severity_model.predict([text])[0]
        is_fallback = False
    else:
        predicted_category = _rule_based_category(text)
        predicted_severity = _rule_based_severity(text)
        confidence = 0.6
        is_fallback = True
        logger.warning("Using rule-based fallback for classification")

    # Safety risk — always rule-based
    safety_risk = _detect_safety_risk(text)

    # Keywords
    keywords = _extract_keywords(text)

    return ClassificationResponse(
        predictedCategory=predicted_category,
        predictedSeverity=predicted_severity,
        confidence=round(confidence, 4),
        keywords=keywords,
        safetyRisk=safety_risk,
        isFallback=is_fallback,
    )
