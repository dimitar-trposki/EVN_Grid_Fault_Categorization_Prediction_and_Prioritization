from fastapi import FastAPI, HTTPException
from app.schemas import *
from app.services.classifier import classify_fault
from app.services.risk import calculate_risk
from app.services.priority import calculate_priority

app = FastAPI()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/classify", response_model=ClassifyResponse)
def classify(req: ClassifyRequest):
    if len(req.description) < 10:
        raise HTTPException(status_code=422, detail="Description too short")

    result = classify_fault(req.description)

    return ClassifyResponse(**result)


@app.post("/predict-risk", response_model=RiskResponse)
def predict_risk(req: RiskRequest):
    score, level = calculate_risk(req.dict())

    return RiskResponse(
        riskScore=score,
        riskLevel=level,
        contributingFactors=["Auto calculated"]
    )


@app.post("/calculate-priority", response_model=PriorityResponse)
def priority(req: PriorityRequest):
    score, level = calculate_priority(req.dict())

    return PriorityResponse(
        priorityScore=score,
        priorityLevel=level,
        explanation="Auto calculated"
    )
