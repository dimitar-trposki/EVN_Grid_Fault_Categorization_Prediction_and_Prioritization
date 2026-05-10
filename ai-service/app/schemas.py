from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class ApiModel(BaseModel):
    """Base model that accepts both snake_case contract fields and legacy camelCase fields."""

    model_config = ConfigDict(populate_by_name=True)


class HealthResponse(ApiModel):
    status: str


class ClassificationRequest(ApiModel):
    fault_id: int = Field(alias="faultId")
    description: str
    location_text: Optional[str] = Field(default=None, alias="locationText")


class ClassificationResponse(ApiModel):
    fault_id: int = Field(alias="faultId")
    predicted_category: str = Field(alias="predictedCategory")
    predicted_severity: str = Field(alias="predictedSeverity")
    confidence: float = Field(ge=0.0, le=1.0)
    keywords: list[str]
    safety_risk: bool = Field(alias="safetyRisk")
    is_fallback: bool = Field(alias="isFallback")


class RiskPredictionRequest(ApiModel):
    location_id: Optional[int] = Field(default=None, alias="locationId")
    equipment_id: Optional[int] = Field(default=None, alias="equipmentId")
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    criticality_level: Optional[str] = Field(default=None, alias="criticalityLevel")
    recent_fault_count: Optional[int] = Field(default=None, alias="recentFaultCount")
    equipment_age_years: Optional[int] = Field(default=None, alias="equipmentAgeYears")
    weather_temperature: Optional[float] = Field(default=None, alias="weatherTemperature")
    weather_wind_speed: Optional[float] = Field(default=None, alias="weatherWindSpeed")
    weather_precipitation: Optional[float] = Field(default=None, alias="weatherPrecipitation")
    weather_condition: Optional[str] = Field(default=None, alias="weatherCondition")


class RiskPredictionResponse(ApiModel):
    risk_score: float = Field(alias="riskScore")
    risk_level: str = Field(alias="riskLevel")
    contributing_factors: list[str] = Field(alias="contributingFactors")
    is_fallback: bool = Field(alias="isFallback")


class PriorityCalculationRequest(ApiModel):
    fault_id: int = Field(alias="faultId")
    fault_category: str = Field(alias="faultCategory")
    severity: str
    safety_risk: bool = Field(alias="safetyRisk")
    affected_users_estimate: Optional[int] = Field(default=0, alias="affectedUsersEstimate")
    location_criticality: Optional[str] = Field(default=None, alias="locationCriticality")
    weather_condition: Optional[str] = Field(default=None, alias="weatherCondition")
    is_recurring: Optional[bool] = Field(default=False, alias="isRecurring")


class PriorityCalculationResponse(ApiModel):
    priority_score: float = Field(alias="priorityScore")
    priority_level: str = Field(alias="priorityLevel")
    explanation: str
    is_fallback: bool = Field(alias="isFallback")

# from __future__ import annotations
#
# from typing import List, Optional
#
# from pydantic import BaseModel
#
#
# class HealthResponse(BaseModel):
#     """Liveness check response for GET /health."""
#
#     status: str
#
#
# class ClassificationRequest(BaseModel):
#     """Request body for POST /classify."""
#
#     faultId: int
#     description: str
#     locationText: Optional[str] = None
#
#
# class ClassificationResponse(BaseModel):
#     """Response body for POST /classify."""
#
#     predictedCategory: str
#     predictedSeverity: str
#     confidence: float
#     keywords: List[str]
#     safetyRisk: bool
#     isFallback: bool = False
#
#
# class RiskPredictionRequest(BaseModel):
#     """Request body for POST /predict-risk.
#
#     equipmentId and the feature fields are optional so that the service can
#     return a sensible LOW-risk baseline when only location co-ordinates are
#     provided (e.g. for new locations with no history).
#     """
#
#     locationId: int
#     equipmentId: Optional[int] = None
#     latitude: float
#     longitude: float
#     criticalityLevel: Optional[int] = None
#     recentFaultCount: Optional[int] = None
#     equipmentAgeYears: Optional[float] = None
#     weatherTemperature: Optional[float] = None
#     weatherWindSpeed: Optional[float] = None
#     weatherPrecipitation: Optional[float] = None
#     weatherCondition: Optional[str] = None
#
#
# class RiskPredictionResponse(BaseModel):
#     """Response body for POST /predict-risk."""
#
#     riskScore: float
#     riskLevel: str
#     contributingFactors: List[str]
#     isFallback: bool = False
#
#
# class PriorityCalculationRequest(BaseModel):
#     """Request body for POST /calculate-priority."""
#
#     faultId: int
#     faultCategory: str
#     severity: str
#     safetyRisk: bool
#     affectedUsersEstimate: int
#     locationCriticality: str
#     weatherCondition: Optional[str] = None
#     isRecurring: bool
#
#
# class PriorityCalculationResponse(BaseModel):
#     """Response body for POST /calculate-priority."""
#
#     priorityScore: float
#     priorityLevel: str
#     explanation: str
#     isFallback: bool = False
