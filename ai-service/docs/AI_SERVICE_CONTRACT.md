# AI Service Contract

This document defines the HTTP API contract for the Python AI microservice.
The Spring Boot backend (`AiClient.java`) is the sole consumer of this API.

- **Base URL (dev):** `http://localhost:8000`
- **Content-Type:** `application/json`
- **Timeout:** 10 seconds connect + read (configurable via `ai.service.timeout-seconds`)

---

## Endpoints

### 1. POST /classify

Classifies a fault report by category and severity based on its description and location context.

**Request:**
```json
{
  "faultId": 42,
  "description": "Transformer overheating at substation, burning smell reported by nearby residents",
  "locationText": "Substation Skopje North, Region Skopje"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| faultId | Long | Yes | ID of the fault report |
| description | String | Yes | Full fault description text |
| locationText | String | Yes | Human-readable location string |

**Response (200 OK):**
```json
{
  "predictedCategory": "ELECTRICAL_FAILURE",
  "predictedSeverity": "HIGH",
  "confidence": 0.87,
  "keywords": ["transformer", "overheating", "burning"],
  "safetyRisk": true,
  "isFallback": false
}
```

| Field | Type | Description |
|-------|------|-------------|
| predictedCategory | String | Fault category (should match a FaultType enum value) |
| predictedSeverity | String | One of: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| confidence | Double | Model confidence 0.0–1.0 |
| keywords | Array\<String\> | Keywords extracted from the description |
| safetyRisk | Boolean | Whether the fault poses an immediate safety risk |
| isFallback | Boolean | Always `false` for real responses; Spring sets `true` on fallback |

**Error responses:**
- `400 Bad Request` — missing required fields
- `422 Unprocessable Entity` — description too short to classify
- `500 Internal Server Error` — model inference error

---

### 2. POST /predict-risk

Predicts the risk level for a location/equipment combination, incorporating optional weather context.

**Request:**
```json
{
  "locationId": 15,
  "equipmentId": 7,
  "latitude": 41.9981,
  "longitude": 21.4254,
  "criticalityLevel": 3,
  "recentFaultCount": 4,
  "equipmentAgeYears": 12.5,
  "weatherTemperature": 34.2,
  "weatherWindSpeed": 45.0,
  "weatherPrecipitation": 0.0,
  "weatherCondition": "SUNNY"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| locationId | Long | Yes | Location identifier |
| equipmentId | Long | Yes | Primary equipment identifier |
| latitude | Double | Yes | Location latitude |
| longitude | Double | Yes | Location longitude |
| criticalityLevel | Integer | Yes | Location/equipment criticality on a 1–5 scale |
| recentFaultCount | Integer | Yes | Number of faults at this location in the past 30 days |
| equipmentAgeYears | Double | Yes | Age of the equipment in years |
| weatherTemperature | Double | **No** | Current temperature (°C); `null` if unavailable |
| weatherWindSpeed | Double | **No** | Current wind speed (km/h); `null` if unavailable |
| weatherPrecipitation | Double | **No** | Current precipitation (mm); `null` if unavailable |
| weatherCondition | String | **No** | One of: `SUNNY`, `CLOUDY`, `RAINY`, `SNOW`, `STORM`; `null` if unavailable |

> **Note on weather fields:** All four weather fields are optional. Module 13 (Weather Integration) supplies them when a recent reading exists for the location; the Python service must handle `null` / absent weather fields gracefully and apply reasonable defaults or reduce the weather feature weight when the data is missing.

**Response (200 OK):**
```json
{
  "riskScore": 78.3,
  "riskLevel": "HIGH",
  "contributingFactors": [
    "High equipment age (12.5 years)",
    "4 recent faults in area",
    "Elevated wind speed (45 km/h)"
  ],
  "isFallback": false
}
```

| Field | Type | Description |
|-------|------|-------------|
| riskScore | Double | Numeric risk score 0–100 |
| riskLevel | String | One of: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| contributingFactors | Array\<String\> | Human-readable factors that drove the score |
| isFallback | Boolean | Always `false` for real responses |

**Error responses:**
- `400 Bad Request` — missing required fields
- `500 Internal Server Error` — model inference error

---

### 3. POST /calculate-priority

Calculates a dispatch priority score for a fault, combining severity, safety risk, affected users, and weather context.

**Request:**
```json
{
  "faultId": 42,
  "faultCategory": "ELECTRICAL_FAILURE",
  "severity": "HIGH",
  "safetyRisk": true,
  "affectedUsersEstimate": 350,
  "locationCriticality": "HIGH",
  "weatherCondition": "STORM",
  "isRecurring": false
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| faultId | Long | Yes | Fault report ID |
| faultCategory | String | Yes | Fault category (matches FaultType enum) |
| severity | String | Yes | One of: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| safetyRisk | Boolean | Yes | Whether there is an immediate safety risk |
| affectedUsersEstimate | Integer | Yes | Estimated number of affected customers |
| locationCriticality | String | Yes | Location criticality label |
| weatherCondition | String | No | Current weather condition; `null` if unavailable |
| isRecurring | Boolean | Yes | Whether this fault type has recurred at this location |

**Response (200 OK):**
```json
{
  "priorityScore": 91.5,
  "priorityLevel": "CRITICAL",
  "explanation": "Safety risk combined with storm conditions and 350 affected users elevates priority to CRITICAL.",
  "isFallback": false
}
```

| Field | Type | Description |
|-------|------|-------------|
| priorityScore | Double | Priority score 0–100 |
| priorityLevel | String | One of: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| explanation | String | Human-readable explanation of the score |
| isFallback | Boolean | Always `false` for real responses |

**Error responses:**
- `400 Bad Request` — missing required fields
- `500 Internal Server Error` — model inference error

---

### 4. GET /health

Simple liveness check. `AiHealthCheckService` polls this endpoint and caches the result for 30 seconds.

**Request:** No body.

**Response (200 OK):**
```json
{
  "status": "ok"
}
```

**Expected behavior:** Return `200` immediately. No heavy processing. If the service is still loading models, return `503` until ready. The Spring Boot health check caches the result for 30 seconds to avoid flooding this endpoint.

---

## General Notes

### `isFallback` semantics

The `isFallback` field is **never set to `true` by the Python service**. It is set exclusively by the Spring Boot `AiClient` when the Python service is unreachable, times out, or returns an error. This flag is persisted to the database so that operators can identify records that received default AI values and trigger a re-run once the Python service is available.

### Severity / level enumerations

Use these string values consistently across all endpoints:

| Concept | Valid values |
|---------|-------------|
| Severity / risk level / priority level | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| Weather condition | `SUNNY`, `CLOUDY`, `RAINY`, `SNOW`, `STORM` |

These match the Spring Boot enums and are parsed by name.

### Timeout budget

The Spring Boot client enforces a **10-second** connect + read timeout. Aim to respond within **5 seconds** on all endpoints to leave margin for network latency.
