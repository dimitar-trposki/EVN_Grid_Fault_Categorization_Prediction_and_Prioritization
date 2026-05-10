"""End-to-end API tests for the EVN Fault AI Service."""
from __future__ import annotations

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json() == {"status": "ok"}


def test_classify_electrical_safety():
    r = client.post("/classify", json={
        "faultId": 1,
        "description": "Голема искра од трансформатор",
    })
    assert r.status_code == 200
    data = r.json()
    assert data["predictedCategory"] in ("ELECTRICAL", "OTHER")
    assert data["safetyRisk"] is True
    assert data["isFallback"] is False


def test_classify_mechanical():
    r = client.post("/classify", json={
        "faultId": 2,
        "description": "Падна дрво на жица",
    })
    assert r.status_code == 200
    data = r.json()
    assert data["predictedCategory"] in ("MECHANICAL", "ELECTRICAL", "OTHER")
    assert data["isFallback"] is False


def test_risk_high():
    r = client.post("/predict-risk", json={
        "locationId": 1,
        "latitude": 41.9,
        "longitude": 21.4,
        "criticalityLevel": 5,
        "equipmentAgeYears": 25,
        "recentFaultCount": 10,
    })
    assert r.status_code == 200
    data = r.json()
    assert data["riskLevel"] == "HIGH"
    assert data["isFallback"] is False


def test_risk_baseline():
    r = client.post("/predict-risk", json={
        "locationId": 1,
        "latitude": 41.9,
        "longitude": 21.4,
    })
    assert r.status_code == 200
    data = r.json()
    assert data["riskLevel"] == "LOW"
    assert "No significant risk factors detected" in data["contributingFactors"]


def test_priority_critical():
    r = client.post("/calculate-priority", json={
        "faultId": 1,
        "faultCategory": "ELECTRICAL",
        "severity": "CRITICAL",
        "safetyRisk": True,
        "affectedUsersEstimate": 2000,
        "locationCriticality": "HIGH",
        "isRecurring": False,
    })
    assert r.status_code == 200
    data = r.json()
    assert data["priorityLevel"] == "CRITICAL"
    assert data["priorityScore"] >= 80
    assert data["isFallback"] is False


def test_priority_low():
    r = client.post("/calculate-priority", json={
        "faultId": 1,
        "faultCategory": "OTHER",
        "severity": "LOW",
        "safetyRisk": False,
        "affectedUsersEstimate": 0,
        "locationCriticality": "LOW",
        "isRecurring": False,
    })
    assert r.status_code == 200
    data = r.json()
    assert data["priorityLevel"] == "LOW"
