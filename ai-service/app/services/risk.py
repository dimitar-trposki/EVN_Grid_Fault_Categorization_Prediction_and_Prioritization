from __future__ import annotations

import logging

from app.schemas import RiskPredictionRequest, RiskPredictionResponse

logger = logging.getLogger(__name__)


def predict(request: RiskPredictionRequest) -> RiskPredictionResponse:
    """Pure weighted-formula risk prediction."""
    score = 0
    factors: list[str] = []

    # Equipment age
    if request.equipmentAgeYears is not None and request.equipmentAgeYears >= 15:
        score += 20
        factors.append("Aged equipment (>15 years)")
    if request.equipmentAgeYears is not None and request.equipmentAgeYears >= 25:
        score += 10
        factors.append("Equipment beyond expected lifespan")

    # Recent fault count
    if request.recentFaultCount is not None and request.recentFaultCount >= 3:
        score += 15
        factors.append("Multiple recent faults at this location")
    if request.recentFaultCount is not None and request.recentFaultCount >= 10:
        score += 15
        factors.append("High fault frequency")

    # Criticality level (integer 1-5 in contract; treat >=4 as HIGH/CRITICAL)
    if request.criticalityLevel is not None and request.criticalityLevel >= 4:
        score += 15
        factors.append("Critical infrastructure location")

    # Wind speed
    if request.weatherWindSpeed is not None and request.weatherWindSpeed > 15:
        score += 10
        factors.append("High wind conditions")
    if request.weatherWindSpeed is not None and request.weatherWindSpeed > 25:
        score += 10
        factors.append("Severe wind storm")

    # Precipitation
    if request.weatherPrecipitation is not None and request.weatherPrecipitation > 10:
        score += 10
        factors.append("Heavy precipitation")

    # Hazardous weather condition (contract uses STORM, SNOW)
    if request.weatherCondition in ("STORM", "THUNDERSTORM", "FREEZING_RAIN"):
        score += 15
        factors.append("Hazardous weather (thunderstorm/freezing rain)")
    elif request.weatherCondition == "SNOW":
        score += 10
        factors.append("Snow conditions")

    # Cap at 100
    score = min(score, 100)

    # Baseline when no factors triggered
    if not factors:
        factors.append("No significant risk factors detected")
        score = 15
        level = "LOW"
    else:
        if score <= 33:
            level = "LOW"
        elif score <= 66:
            level = "MEDIUM"
        else:
            level = "HIGH"

    return RiskPredictionResponse(
        riskScore=float(score),
        riskLevel=level,
        contributingFactors=factors,
        isFallback=False,
    )
