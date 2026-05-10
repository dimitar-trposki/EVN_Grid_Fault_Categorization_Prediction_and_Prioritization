from __future__ import annotations

import logging

from app.schemas import PriorityCalculationRequest, PriorityCalculationResponse

logger = logging.getLogger(__name__)


# Hardcoded mapping of EVN region names to criticality level.
# The backend sends region.getName() as locationCriticality (e.g. "Skopje")
# because Location has no criticalityLevel field.
# Levels: HIGH = major urban / industrial hub, MEDIUM = mid-size city,
#         LOW = smaller town / rural area.
_REGION_CRITICALITY: dict[str, str] = {
    # Major urban centres — HIGH
    "Skopje":       "HIGH",
    "Bitola":       "HIGH",
    "Kumanovo":     "HIGH",
    # Mid-size cities — MEDIUM
    "Prilep":       "MEDIUM",
    "Tetovo":       "MEDIUM",
    "Ohrid":        "MEDIUM",
    "Veles":        "MEDIUM",
    "Strumica":     "MEDIUM",
    "Stip":         "MEDIUM",
    "Štip":         "MEDIUM",
    "Gostivar":     "MEDIUM",
    "Kavadarci":    "MEDIUM",
    "Kočani":       "MEDIUM",
    "Kocani":       "MEDIUM",
    # Smaller towns — LOW
    "Gevgelija":    "LOW",
    "Kičevo":       "LOW",
    "Kicevo":       "LOW",
    "Struga":       "LOW",
    "Negotino":     "LOW",
    "Radoviš":      "LOW",
    "Radovis":      "LOW",
    "Debar":        "LOW",
    "Kratovo":      "LOW",
    "Berovo":       "LOW",
    "Delčevo":      "LOW",
    "Delcevo":      "LOW",
    "Sveti Nikole": "LOW",
    "Resen":        "LOW",
    "Probištip":    "LOW",
    "Probistip":    "LOW",
    "Vinica":       "LOW",
    "Demir Hisar":  "LOW",
    "Kriva Palanka":"LOW",
    "Makedonski Brod":"LOW",
}


def _resolve_criticality(label: str) -> str:
    """Map a locationCriticality value (which may be a region name) to a
    standard level: HIGH, MEDIUM, or LOW."""
    if not label:
        return "LOW"
    upper = label.upper().strip()
    # already a standard level
    if upper in ("HIGH", "CRITICAL", "MEDIUM", "LOW"):
        return upper
    # try region-name lookup (case-insensitive)
    for name, level in _REGION_CRITICALITY.items():
        if name.upper() == upper:
            return level
    # unknown region — default to MEDIUM
    return "MEDIUM"


def _estimate_affected_users(severity: str) -> int:
    """Derive a reasonable affected-users estimate from severity when the
    backend does not provide one (sends 0)."""
    return {
        "CRITICAL": 1000,
        "HIGH": 200,
        "MEDIUM": 50,
        "LOW": 5,
    }.get(severity, 50)


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

    # Affected users — when the backend sends 0 it means "unknown" (the field
    # does not exist on FaultReport yet), so we derive a reasonable estimate
    # from severity to avoid silently dropping this scoring factor.
    affected = request.affectedUsersEstimate
    if affected == 0:
        affected = _estimate_affected_users(request.severity)

    if affected >= 1000:
        score += 30
        factors.append("Major outage (1000+ affected)")
    elif affected >= 100:
        score += 20
        factors.append("Wide impact (100+ affected)")
    elif affected >= 10:
        score += 10

    # Location criticality — the backend sends region.getName() (e.g. "Skopje")
    # instead of a severity label.  Resolve it via the hardcoded region map.
    resolved_crit = _resolve_criticality(request.locationCriticality)
    if resolved_crit in ("HIGH", "CRITICAL"):
        score += 15
        factors.append("Critical location")
    elif resolved_crit == "MEDIUM":
        score += 8
        factors.append("Moderate-criticality location")

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
