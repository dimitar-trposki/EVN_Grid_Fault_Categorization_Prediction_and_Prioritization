# EVN Fault AI Service

Python microservice providing AI-powered fault classification, risk prediction, and priority calculation for the EVN fault management system. Consumed via HTTP by the Spring Boot backend.

## Setup

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
python -m spacy download en_core_web_sm  # optional
```

## Training

```bash
python scripts/generate_synthetic_data.py
python scripts/train_classifier.py
python scripts/train_severity.py
```

## Running

```bash
uvicorn app.main:app --reload --port 8000
```

OpenAPI docs: http://localhost:8000/docs

## Endpoints

- GET /health
- POST /classify
- POST /predict-risk
- POST /calculate-priority

See `docs/AI_SERVICE_CONTRACT.md` for full schemas.

## Testing

```bash
pytest tests/ -v
```

## Docker

```bash
docker build -t fault-ai-service .
docker run -p 8000:8000 fault-ai-service
```
