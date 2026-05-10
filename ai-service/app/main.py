from __future__ import annotations

import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.schemas import (
    ClassificationRequest,
    ClassificationResponse,
    HealthResponse,
    PriorityCalculationRequest,
    PriorityCalculationResponse,
    RiskPredictionRequest,
    RiskPredictionResponse,
)
from app.services import classifier as classifier_service
from app.services import priority as priority_service
from app.services import risk as risk_service

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

app = FastAPI(title="EVN Fault AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=True,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


@app.post("/classify", response_model=ClassificationResponse)
def classify(request: ClassificationRequest) -> ClassificationResponse:
    try:
        return classifier_service.classify(request)
    except Exception:
        logger.error("classify failed", exc_info=True)
        return ClassificationResponse(
            predictedCategory="UNKNOWN",
            predictedSeverity="MEDIUM",
            confidence=0.0,
            keywords=[],
            safetyRisk=False,
            isFallback=True,
        )


@app.post("/predict-risk", response_model=RiskPredictionResponse)
def predict_risk(request: RiskPredictionRequest) -> RiskPredictionResponse:
    try:
        return risk_service.predict(request)
    except Exception:
        logger.error("predict_risk failed", exc_info=True)
        return RiskPredictionResponse(
            riskScore=50.0,
            riskLevel="MEDIUM",
            contributingFactors=["AI service error, fallback applied"],
            isFallback=True,
        )


@app.post("/calculate-priority", response_model=PriorityCalculationResponse)
def calculate_priority(request: PriorityCalculationRequest) -> PriorityCalculationResponse:
    try:
        return priority_service.calculate(request)
    except Exception:
        logger.error("calculate_priority failed", exc_info=True)
        return PriorityCalculationResponse(
            priorityScore=50.0,
            priorityLevel="MEDIUM",
            explanation="AI service error, default priority assigned",
            isFallback=True,
        )
