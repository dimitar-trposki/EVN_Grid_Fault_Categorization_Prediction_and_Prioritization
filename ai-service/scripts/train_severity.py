from __future__ import annotations

from pathlib import Path
import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline

ROOT = Path(__file__).resolve().parents[1]
DATA_PATH = ROOT / "data" / "synthetic_faults.csv"
MODEL_PATH = ROOT / "models" / "severity.joblib"


def main() -> None:
    df = pd.read_csv(DATA_PATH)
    print(f"Rows: {len(df)}")
    print("Category distribution:")
    print(df["category"].value_counts().to_string())
    print("Severity distribution:")
    print(df["severity"].value_counts().to_string())
    print(f"Missing values: {int(df.isna().sum().sum())}")

    x_train, x_test, y_train, y_test = train_test_split(
        df["description"],
        df["severity"],
        test_size=0.2,
        stratify=df["category"],
        random_state=42,
    )
    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            max_features=5000,
            ngram_range=(1, 2),
            lowercase=True,
            min_df=2,
            strip_accents=None,
            token_pattern=r"(?u)\b\w+\b",
        )),
        ("logreg", LogisticRegression(max_iter=1000, class_weight="balanced", random_state=42)),
    ])
    pipeline.fit(x_train, y_train)
    predictions = pipeline.predict(x_test)
    print("Classification report:")
    print(classification_report(y_test, predictions, zero_division=0))
    print("Confusion matrix:")
    print(confusion_matrix(y_test, predictions, labels=sorted(df["severity"].unique())))
    print(f"Accuracy: {accuracy_score(y_test, predictions):.4f}")
    MODEL_PATH.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(pipeline, MODEL_PATH)
    print(f"Saved model to {MODEL_PATH} ({MODEL_PATH.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
