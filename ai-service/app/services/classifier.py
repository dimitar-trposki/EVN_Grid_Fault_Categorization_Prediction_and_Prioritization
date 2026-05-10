def classify(request):
    raise NotImplementedError("Classifier not yet implemented")


def classify_fault(description: str):
    desc = description.lower()

    keywords = []
    if "transformer" in desc:
        keywords.append("transformer")
    if "burn" in desc or "overheat" in desc:
        keywords.append("overheating")

    severity = "LOW"
    if "fire" in desc or "burn" in desc:
        severity = "CRITICAL"

    return {
        "predictedCategory": "ELECTRICAL_FAILURE",
        "predictedSeverity": severity,
        "confidence": 0.85,
        "keywords": keywords,
        "safetyRisk": severity in ["HIGH", "CRITICAL"]
    }
