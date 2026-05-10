from __future__ import annotations

import logging
from app.schemas import RiskPredictionRequest, RiskPredictionResponse

logger = logging.getLogger(__name__)


def _norm(value: str | None) -> str:
    return (value or "").strip().upper()


def predict(request: RiskPredictionRequest) -> RiskPredictionResponse:
    try:
        score = 0
        factors: list[str] = []

        if request.equipment_age_years is not None and request.equipment_age_years >= 15:
            score += 20
            factors.append("Aged equipment (>15 years)")
        if request.equipment_age_years is not None and request.equipment_age_years >= 25:
            score += 10
            factors.append("Equipment beyond expected lifespan")
        if request.recent_fault_count is not None and request.recent_fault_count >= 3:
            score += 15
            factors.append("Multiple recent faults at this location")
        if request.recent_fault_count is not None and request.recent_fault_count >= 10:
            score += 15
            factors.append("High fault frequency")
        if _norm(request.criticality_level) in ("HIGH", "CRITICAL"):
            score += 15
            factors.append("Critical infrastructure location")
        if request.weather_wind_speed is not None and request.weather_wind_speed > 15:
            score += 20
            factors.append("High wind conditions")
        if request.weather_wind_speed is not None and request.weather_wind_speed > 25:
            score += 10
            factors.append("Severe wind storm")
        if request.weather_precipitation is not None and request.weather_precipitation > 10:
            score += 10
            factors.append("Heavy precipitation")
        if _norm(request.weather_condition) in ("THUNDERSTORM", "FREEZING_RAIN"):
            score += 15
            factors.append("Hazardous weather (thunderstorm/freezing rain)")
        if _norm(request.weather_condition) == "SNOW":
            score += 10
            factors.append("Snow conditions")

        score = min(score, 100)

        if not factors:
            factors.append("No significant risk factors detected")
            score = 15
            level = "LOW"
        elif score <= 33:
            level = "LOW"
        elif score <= 66:
            level = "MEDIUM"
        else:
            level = "HIGH"

        return RiskPredictionResponse(
            risk_score=float(score),
            risk_level=level,
            contributing_factors=factors,
            is_fallback=False,
        )
    except Exception as exc:  # pragma: no cover - defensive infrastructure fallback
        logger.exception("Risk prediction failed: %s", exc)
        return RiskPredictionResponse(
            risk_score=15.0,
            risk_level="LOW",
            contributing_factors=["No significant risk factors detected"],
            is_fallback=True,
        )

# def predict(request):
#     raise NotImplementedError("Risk predictor not yet implemented")
#
#
# def calculate_risk(data):
#     score = 0
#
#     score += data["criticalityLevel"] * 10
#     score += data["recentFaultCount"] * 5
#     score += data["equipmentAgeYears"] * 1.5
#
#     if data.get("weatherWindSpeed"):
#         score += data["weatherWindSpeed"] * 0.5
#
#     if score > 80:
#         level = "CRITICAL"
#     elif score > 60:
#         level = "HIGH"
#     elif score > 40:
#         level = "MEDIUM"
#     else:
#         level = "LOW"
#
#     return score, level
