from __future__ import annotations

import logging
from app.schemas import PriorityCalculationRequest, PriorityCalculationResponse

logger = logging.getLogger(__name__)


def _norm(value: str | None) -> str:
    return (value or "").strip().upper()


def calculate(request: PriorityCalculationRequest) -> PriorityCalculationResponse:
    try:
        score = 0
        factors: list[str] = []
        severity = _norm(request.severity)

        if request.safety_risk:
            score += 40
            factors.append("Safety risk identified")
        if severity == "CRITICAL":
            score += 30
            factors.append("Critical severity")
        elif severity == "HIGH":
            score += 20
            factors.append("High severity")
        elif severity == "MEDIUM":
            score += 10

        affected_users = request.affected_users_estimate or 0
        if affected_users >= 1000:
            score += 30
            factors.append("Major outage (1000+ affected)")
        elif affected_users >= 100:
            score += 20
            factors.append("Wide impact (100+ affected)")
        elif affected_users >= 10:
            score += 10

        if _norm(request.location_criticality) in ("HIGH", "CRITICAL"):
            score += 15
            factors.append("Critical location")
        if _norm(request.weather_condition) in ("THUNDERSTORM", "FREEZING_RAIN", "SNOW"):
            score += 10
            factors.append("Adverse weather")
        if request.is_recurring:
            score += 10
            factors.append("Recurring problem")

        score = min(score, 100)
        if score == 0:
            score = 25
            factors = ["Standard priority"]

        if score <= 25:
            level = "LOW"
        elif score <= 50:
            level = "MEDIUM"
        elif score <= 75:
            level = "HIGH"
        else:
            level = "CRITICAL"

        return PriorityCalculationResponse(
            priority_score=float(score),
            priority_level=level,
            explanation=", ".join(factors),
            is_fallback=False,
        )
    except Exception as exc:  # pragma: no cover
        logger.exception("Priority calculation failed: %s", exc)
        return PriorityCalculationResponse(
            priority_score=25.0,
            priority_level="LOW",
            explanation="Standard priority",
            is_fallback=True,
        )

# def calculate(request):
#     raise NotImplementedError("Priority calculator not yet implemented")
#
#
# def calculate_priority(data):
#     score = 0
#
#     severity_map = {
#         "LOW": 10,
#         "MEDIUM": 30,
#         "HIGH": 60,
#         "CRITICAL": 90
#     }
#
#     score += severity_map.get(data["severity"], 0)
#
#     if data["safetyRisk"]:
#         score += 20
#
#     score += data["affectedUsersEstimate"] * 0.05
#
#     if data["weatherCondition"] == "STORM":
#         score += 15
#
#     if score > 90:
#         level = "CRITICAL"
#     elif score > 70:
#         level = "HIGH"
#     elif score > 40:
#         level = "MEDIUM"
#     else:
#         level = "LOW"
#
#     return score, level
