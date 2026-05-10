from pydantic import BaseModel
from typing import List, Optional


# CLASSIFY
class ClassifyRequest(BaseModel):
    faultId: int
    description: str
    locationText: str


class ClassifyResponse(BaseModel):
    predictedCategory: str
    predictedSeverity: str
    confidence: float
    keywords: List[str]
    safetyRisk: bool
    isFallback: bool = False


# RISK
class RiskRequest(BaseModel):
    locationId: int
    equipmentId: int
    latitude: float
    longitude: float
    criticalityLevel: int
    recentFaultCount: int
    equipmentAgeYears: float
    weatherTemperature: Optional[float] = None
    weatherWindSpeed: Optional[float] = None
    weatherPrecipitation: Optional[float] = None
    weatherCondition: Optional[str] = None


class RiskResponse(BaseModel):
    riskScore: float
    riskLevel: str
    contributingFactors: List[str]
    isFallback: bool = False


# PRIORITY
class PriorityRequest(BaseModel):
    faultId: int
    faultCategory: str
    severity: str
    safetyRisk: bool
    affectedUsersEstimate: int
    locationCriticality: str
    weatherCondition: Optional[str] = None
    isRecurring: bool


class PriorityResponse(BaseModel):
    priorityScore: float
    priorityLevel: str
    explanation: str
    isFallback: bool = False
