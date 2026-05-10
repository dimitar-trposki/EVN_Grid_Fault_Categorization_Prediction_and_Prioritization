def calculate_risk(data):
    score = 0

    score += data["criticalityLevel"] * 10
    score += data["recentFaultCount"] * 5
    score += data["equipmentAgeYears"] * 1.5

    if data.get("weatherWindSpeed"):
        score += data["weatherWindSpeed"] * 0.5

    if score > 80:
        level = "CRITICAL"
    elif score > 60:
        level = "HIGH"
    elif score > 40:
        level = "MEDIUM"
    else:
        level = "LOW"

    return score, level
