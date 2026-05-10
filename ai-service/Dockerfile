FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN python -m spacy download en_core_web_sm || true

COPY app/ ./app/
COPY scripts/ ./scripts/
COPY data/ ./data/
COPY models/ ./models/

# Generate data and train models at build time if missing (idempotent)
RUN python scripts/generate_synthetic_data.py || true
RUN python scripts/train_classifier.py || true
RUN python scripts/train_severity.py || true

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
