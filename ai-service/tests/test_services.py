from __future__ import annotations

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_classify_electrical_safety() -> None:
    response = client.post(
        "/classify",
        json={"fault_id": 1, "description": "Голема искра од трансформатор"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["predicted_category"] in ("ELECTRICAL", "OTHER")
    assert body["safety_risk"] is True


def test_classify_mechanical() -> None:
    response = client.post(
        "/classify",
        json={"fault_id": 2, "description": "Падна дрво на жица"},
    )
    assert response.status_code == 200
    assert response.json()["predicted_category"] in ("MECHANICAL", "ELECTRICAL", "OTHER")


def test_risk_high() -> None:
    response = client.post(
        "/predict-risk",
        json={
            "location_id": 1,
            "latitude": 41.9,
            "longitude": 21.4,
            "criticality_level": "HIGH",
            "recent_fault_count": 10,
            "equipment_age_years": 25,
        },
    )
    assert response.status_code == 200
    assert response.json()["risk_level"] == "HIGH"


def test_risk_baseline() -> None:
    response = client.post(
        "/predict-risk",
        json={"location_id": 1, "latitude": 41.9, "longitude": 21.4},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["risk_level"] == "LOW"
    assert "No significant risk factors detected" in body["contributing_factors"]


def test_priority_critical() -> None:
    response = client.post(
        "/calculate-priority",
        json={
            "fault_id": 1,
            "fault_category": "ELECTRICAL",
            "severity": "CRITICAL",
            "safety_risk": True,
            "affected_users_estimate": 2000,
            "location_criticality": "HIGH",
            "is_recurring": False,
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["priority_level"] == "CRITICAL"
    assert body["priority_score"] >= 80


def test_priority_low() -> None:
    response = client.post(
        "/calculate-priority",
        json={
            "fault_id": 1,
            "fault_category": "OTHER",
            "severity": "LOW",
            "safety_risk": False,
            "affected_users_estimate": 0,
            "location_criticality": "LOW",
            "is_recurring": False,
        },
    )
    assert response.status_code == 200
    assert response.json()["priority_level"] == "LOW"
