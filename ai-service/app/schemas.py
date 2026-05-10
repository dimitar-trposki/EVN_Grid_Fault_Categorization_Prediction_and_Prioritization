from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel


class HealthResponse(BaseModel):
    """Liveness check response for GET /health."""

    status: str


class ClassificationRequest(BaseModel):
    """Request body for POST /classify."""

    faultId: int
    description: str
    locationText: Optional[str] = None


class ClassificationResponse(BaseModel):
    """Response body for POST /classify."""

    predictedCategory: str
    predictedSeverity: str
    confidence: float
    keywords: List[str]
    safetyRisk: bool
    isFallback: bool = False


class RiskPredictionRequest(BaseModel):
    """Request body for POST /predict-risk.

    equipmentId and the feature fields are optional so that the service can
    return a sensible LOW-risk baseline when only location co-ordinates are
    provided (e.g. for new locations with no history).
    """

    locationId: int
    equipmentId: Optional[int] = None
    latitude: float
    longitude: float
    criticalityLevel: Optional[int] = None
    recentFaultCount: Optional[int] = None
    equipmentAgeYears: Optional[float] = None
    weatherTemperature: Optional[float] = None
    weatherWindSpeed: Optional[float] = None
    weatherPrecipitation: Optional[float] = None
    weatherCondition: Optional[str] = None


class RiskPredictionResponse(BaseModel):
    """Response body for POST /predict-risk."""

    riskScore: float
    riskLevel: str
    contributingFactors: List[str]
    isFallback: bool = False


class PriorityCalculationRequest(BaseModel):
    """Request body for POST /calculate-priority."""

    faultId: int
    faultCategory: str
    severity: str
    safetyRisk: bool
    affectedUsersEstimate: int
    locationCriticality: str
    weatherCondition: Optional[str] = None
    isRecurring: bool


class PriorityCalculationResponse(BaseModel):
    """Response body for POST /calculate-priority."""

    priorityScore: float
    priorityLevel: str
    explanation: str
    isFallback: bool = False
