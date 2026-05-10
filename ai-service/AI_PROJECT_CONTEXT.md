Ai project context · MD
Copy

# AI Service — Project Context

> This file is the source of truth for the Python AI microservice. Read it at the start of every session.
> The HTTP contract that this service must implement is in `docs/AI_SERVICE_CONTRACT.md` — the Spring Boot backend
> ALREADY calls these endpoints with this exact request/response schema. Do not change the contract. Implement what it
> specifies.
 
---

## 1. Project Overview

This is a Python microservice that provides three AI capabilities to the Spring Boot backend (a fault management system
for an electricity distribution network):

1. **Fault classification** — given a fault description text, return predicted category, severity, confidence, extracted
   keywords, and safety risk flag.
2. **Risk prediction** — given a location/equipment context (with optional weather), return a risk score, level, and
   contributing factors.
3. **Priority calculation** — given a fault context, return a priority score, level, and human-readable explanation.
   The service is consumed via HTTP by Spring Boot. It does NOT access the database directly. Spring Boot sends all
   needed context in the request body.

---

## 2. Tech Stack

- **Framework**: FastAPI
- **Server**: uvicorn
- **ML / NLP**: scikit-learn (classification, simple risk model), spaCy (text analysis, keyword extraction)
- **Validation**: Pydantic v2 (built into FastAPI)
- **Python**: 3.11+

---

## 3. Project Structure

```
ai-service/
├── .venv/                    # virtualenv (do not commit)
├── app/
│   ├── main.py               # FastAPI app, routes, startup
│   ├── schemas.py            # Pydantic request/response models
│   └── services/
│       ├── classifier.py     # classification logic (NLP + ML)
│       ├── risk.py           # risk prediction logic
│       └── priority.py       # priority calculation logic
├── docs/
│   └── AI_SERVICE_CONTRACT.md  # the HTTP contract — IMPLEMENT THIS
├── models/                   # serialized ML models (created at training time)
├── data/                     # synthetic training data (created when needed)
├── tests/                    # pytest tests
├── requirements.txt
├── Dockerfile
└── AI_PROJECT_CONTEXT.md     # this file
```

 
---

## 4. Coding Conventions

- **Pydantic models**: define all request/response schemas in `schemas.py`. Field names must match the contract
  exactly (typically snake_case).
- **Routes**: defined in `main.py`. Each route delegates to a service function — no logic in route handlers.
- **Services**: pure Python functions or small classes in `app/services/`. Each takes typed input, returns typed output,
  no FastAPI dependencies.
- **Async**: routes can be async, but services can be sync (sklearn/spaCy are CPU-bound, async doesn't help). Use `def`
  not `async def` for service functions.
- **Logging**: use Python's `logging` module. INFO for normal ops, WARNING for fallback decisions, ERROR for failures.
- **Type hints**: required everywhere. Use `from __future__ import annotations` if needed.
- **Error handling**: never let an exception bubble up unhandled. The Spring Boot client expects a 200 response with
  sensible content even when the model is uncertain. Only return 5xx for genuine infrastructure failures.

---

## 5. Implementation Strategy (Pragmatic, Not Perfect)

This is a student/demo project. The goal is functional AI that demonstrates the three capabilities convincingly, not
state-of-the-art models. Prefer simple, reliable approaches over complex ones.

### Classification (`classifier.py`)

- **Input**: fault description text (Macedonian or English), optional location text.
- **Output**: predicted category (one of: ELECTRICAL, MECHANICAL, SOFTWARE, NETWORK, OTHER), severity (
  CRITICAL/HIGH/MEDIUM/LOW), confidence (0.0–1.0), extracted keywords (list of strings), safety risk (boolean).
- **Approach (in order of pragmatism)**:
    1. **Rule-based first**: keyword matching (e.g. "spark", "fire", "explosion" → safety risk + HIGH severity; "
       transformer", "voltage" → ELECTRICAL; "tree", "branch" → MECHANICAL).
    2. **TF-IDF + LogisticRegression** trained on synthetic data (generate ~500 labeled examples in `data/`).
    3. **spaCy** for keyword extraction: noun chunks, named entities.
- **Confidence**: from `model.predict_proba` for the predicted class. If using rule-based fallback, set confidence to
  0.6.

### Risk Prediction (`risk.py`)

- **Input**: location_id, equipment_id (one of them present), latitude, longitude, criticality_level,
  recent_fault_count, equipment_age_years, weather_temperature, weather_wind_speed, weather_precipitation,
  weather_condition (all weather fields optional).
- **Output**: risk_score (0–100), risk_level (LOW/MEDIUM/HIGH), contributing_factors (list of strings).
- **Approach**:
    - **Weighted formula**: each input contributes a weighted amount to the score.
    - Equipment age > 15 years → +20 to score, factor "Aged equipment".
    - Recent fault count > 5 in last 30 days → +25 to score, factor "Recurring faults at this location".
    - Critical location → +15 to score, factor "Critical infrastructure location".
    - Wind speed > 15 m/s OR precipitation > 10mm OR weather_condition in {THUNDERSTORM, FREEZING_RAIN, SNOW} → +20 to
      score, factor "Severe weather conditions".
    - Score → level: 0–33 LOW, 34–66 MEDIUM, 67–100 HIGH.
- A trained ML model is overkill here for synthetic data. Stick with the formula and document it as the intentional
  choice.

### Priority Calculation (`priority.py`)

- **Input**: fault_id, fault_category, severity, safety_risk, affected_users_estimate, location_criticality,
  weather_condition, is_recurring.
- **Output**: priority_score (0–100), priority_level (CRITICAL/HIGH/MEDIUM/LOW), explanation (human-readable string).
- **Approach**: weighted formula, similar to risk.
    - Safety risk = true → +40 to score, "Safety risk identified".
    - Severity CRITICAL → +30; HIGH → +20; MEDIUM → +10.
    - Affected users > 100 → +20; > 1000 → +30.
    - Critical location → +15.
    - Severe weather → +10.
    - Recurring → +10.
    - Score → level: 0–25 LOW, 26–50 MEDIUM, 51–75 HIGH, 76–100 CRITICAL.
    - Explanation = comma-joined list of all triggered factors.

---

## 6. Health Endpoint

GET /health → `{"status": "ok"}`. No logic. Used by Spring Boot's AiHealthCheckService.
 
---

## 7. Synthetic Training Data

For classification, generate synthetic training examples in `data/synthetic_faults.csv`:

- ~500 rows
- Columns: description, category, severity, safety_risk
- Mix of Macedonian and English phrases (the system runs in MK)
- Examples per category: 100 ELECTRICAL, 100 MECHANICAL, 80 NETWORK, 80 SOFTWARE, 140 OTHER
  A small script `scripts/generate_synthetic_data.py` can produce this. Train the classifier once at startup (or via a
  separate `train.py` script that produces `models/classifier.joblib`).

---

## 8. Don'ts

- DO NOT change the HTTP contract in `docs/AI_SERVICE_CONTRACT.md`. Spring Boot already speaks this contract.
- DO NOT call back to Spring Boot or any database. The service is stateless.
- DO NOT add authentication. The service runs on a private network behind Spring Boot.
- DO NOT use heavy models (transformers, BERT). Overkill for the use case and slow on CPU.
- DO NOT raise exceptions to the client. Catch, log, return a sensible default with low confidence.

---

## 9. Build Order

1. Read `docs/AI_SERVICE_CONTRACT.md` carefully.
2. Define all Pydantic schemas in `schemas.py` matching the contract exactly.
3. Implement `main.py` with all endpoints, each delegating to a service.
4. Implement `services/classifier.py` (rule-based MVP first, then TF-IDF if time).
5. Implement `services/risk.py` (formula).
6. Implement `services/priority.py` (formula).
7. Add `requirements.txt`.
8. Add `Dockerfile`.
9. Test locally with curl/Postman against the Spring Boot AiClient expectations.
10. Optional: write a few pytest tests for each service.