#!/usr/bin/env python3
"""
Train the fault-category classifier (TF-IDF + LogisticRegression).
Input:  data/synthetic_faults.csv
Output: models/classifier.joblib
"""
import sys
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = Path(__file__).resolve().parent.parent
DATA_PATH = ROOT / "data" / "synthetic_faults.csv"
MODEL_PATH = ROOT / "models" / "classifier.joblib"

# ── 1. Load data ──────────────────────────────────────────────────
df = pd.read_csv(DATA_PATH, encoding="utf-8-sig")
print(f"Loaded {len(df)} rows from {DATA_PATH}")

# ── 2. Verify data ───────────────────────────────────────────────
print(f"\nRow count: {len(df)}")
print("\nCategory distribution:")
print(df["category"].value_counts().to_string())
print("\nSeverity distribution:")
print(df["severity"].value_counts().to_string())
print(f"\nMissing values:\n{df.isnull().sum().to_string()}")

# ── 3. Train/test split ──────────────────────────────────────────
X = df["description"]
y = df["category"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, stratify=y, random_state=42
)
print(f"\nTrain: {len(X_train)}, Test: {len(X_test)}")

# ── 4. Build pipeline ────────────────────────────────────────────
pipeline = Pipeline([
    ("tfidf", TfidfVectorizer(
        max_features=5000,
        ngram_range=(1, 2),
        lowercase=True,
        min_df=2,
        strip_accents=None,
        token_pattern=r"(?u)\b\w+\b",
    )),
    ("clf", LogisticRegression(
        max_iter=1000,
        class_weight="balanced",
        random_state=42,
    )),
])

# ── 5. Fit ────────────────────────────────────────────────────────
pipeline.fit(X_train, y_train)

# ── 6. Evaluate ──────────────────────────────────────────────────
y_pred = pipeline.predict(X_test)
print("\n=== Classification Report ===")
print(classification_report(y_test, y_pred))
print("=== Confusion Matrix ===")
print(confusion_matrix(y_test, y_pred))
print(f"\nOverall accuracy: {accuracy_score(y_test, y_pred):.4f}")

# ── 7. Save ──────────────────────────────────────────────────────
MODEL_PATH.parent.mkdir(exist_ok=True)
joblib.dump(pipeline, MODEL_PATH)
size_kb = MODEL_PATH.stat().st_size / 1024
print(f"\nModel saved to {MODEL_PATH} ({size_kb:.1f} KB)")
