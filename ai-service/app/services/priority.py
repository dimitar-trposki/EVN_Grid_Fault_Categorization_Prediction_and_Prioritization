from __future__ import annotations

import logging

from app.schemas import PriorityCalculationRequest, PriorityCalculationResponse

logger = logging.getLogger(__name__)


def calculate(request: PriorityCalculationRequest) -> PriorityCalculationResponse:
    """Pure weighted-formula priority calculation."""
    score = 0
    factors: list[str] = []

    # Safety risk
    if request.safetyRisk:
        score += 40
        factors.append("Safety risk identified")

    # Severity
    if request.severity == "CRITICAL":
        score += 30
        factors.append("Critical severity")
    elif request.severity == "HIGH":
        score += 20
        factors.append("High severity")
    elif request.severity == "MEDIUM":
        score += 10

    # Affected users (check larger threshold first, but apply only one)
    if request.affectedUsersEstimate >= 1000:
        score += 30
        factors.append("Major outage (1000+ affected)")
    elif request.affectedUsersEstimate >= 100:
        score += 20
        factors.append("Wide impact (100+ affected)")
    elif request.affectedUsersEstimate >= 10:
        score += 10

    # Location criticality (string label from Spring Boot)
    if request.locationCriticality in ("HIGH", "CRITICAL"):
        score += 15
        factors.append("Critical location")

    # Weather (contract uses STORM, SNOW)
    if request.weatherCondition in ("STORM", "THUNDERSTORM", "FREEZING_RAIN", "SNOW"):
        score += 10
        factors.append("Adverse weather")

    # Recurring
    if request.isRecurring:
        score += 10
        factors.append("Recurring problem")

    # Cap at 100
    score = min(score, 100)

    # Baseline when no factors triggered (score stayed 0)
    if score == 0:
        score = 25
        factors = ["Standard priority"]

    # Level
    if score <= 25:
        level = "LOW"
    elif score <= 50:
        level = "MEDIUM"
    elif score <= 75:
        level = "HIGH"
    else:
        level = "CRITICAL"

    explanation = ", ".join(factors)

    return PriorityCalculationResponse(
        priorityScore=float(score),
        priorityLevel=level,
        explanation=explanation,
        isFallback=False,
    )
