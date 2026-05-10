def calculate_priority(data):
    score = 0

    severity_map = {
        "LOW": 10,
        "MEDIUM": 30,
        "HIGH": 60,
        "CRITICAL": 90
    }

    score += severity_map.get(data["severity"], 0)

    if data["safetyRisk"]:
        score += 20

    score += data["affectedUsersEstimate"] * 0.05

    if data["weatherCondition"] == "STORM":
        score += 15

    if score > 90:
        level = "CRITICAL"
    elif score > 70:
        level = "HIGH"
    elif score > 40:
        level = "MEDIUM"
    else:
        level = "LOW"

    return score, level
