"""
Train a RandomForest risk prediction model on synthetic grid fault data.

Features used:
    - criticality_level (1-5 scale of location importance)
    - recent_fault_count (number of faults in last 30 days at location)
    - equipment_age_years (age of equipment in years)
    - weather_temperature (Celsius)
    - weather_wind_speed (km/h)
    - weather_precipitation (mm)
    - weather_condition_encoded (0=SUNNY, 1=CLOUDY, 2=RAINY, 3=SNOW, 4=STORM)

Target: risk_score (0-100 continuous)
"""

import os
import numpy as np
import joblib
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

FEATURE_NAMES = [
    "criticality_level",
    "recent_fault_count",
    "equipment_age_years",
    "weather_temperature",
    "weather_wind_speed",
    "weather_precipitation",
    "weather_condition_encoded",
]

WEATHER_ENCODING = {
    "SUNNY": 0,
    "CLOUDY": 1,
    "RAINY": 2,
    "SNOW": 3,
    "STORM": 4,
}

MODEL_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(MODEL_DIR, "risk_model.joblib")


def generate_synthetic_data(n_samples: int = 5000, seed: int = 42) -> tuple:
    """
    Generate synthetic training data that models realistic grid fault risk patterns.

    Risk is higher when:
        - Location criticality is high (hospitals, industrial zones)
        - There are many recent faults (indicates systemic issues)
        - Equipment is old (wear and degradation)
        - Weather is severe (storms, high wind, heavy precipitation)
    """
    rng = np.random.RandomState(seed)

    criticality = rng.randint(1, 6, size=n_samples).astype(float)
    recent_faults = rng.poisson(lam=3, size=n_samples).astype(float)
    equipment_age = rng.exponential(scale=12, size=n_samples).clip(0, 50)
    temperature = rng.normal(loc=15, scale=12, size=n_samples).clip(-20, 45)
    wind_speed = rng.exponential(scale=15, size=n_samples).clip(0, 120)
    precipitation = rng.exponential(scale=5, size=n_samples).clip(0, 80)
    weather_cond = rng.choice([0, 1, 2, 3, 4], size=n_samples, p=[0.3, 0.25, 0.2, 0.1, 0.15])

    X = np.column_stack([
        criticality,
        recent_faults,
        equipment_age,
        temperature,
        wind_speed,
        precipitation,
        weather_cond,
    ])

    # --- Compute target risk score using a realistic model ---

    risk = np.zeros(n_samples)

    # Criticality contributes strongly (1-5 → 5-25 points)
    risk += criticality * 5.0

    # Recent faults indicate systemic issues (0-10+ → 0-30 points)
    risk += np.minimum(recent_faults * 3.0, 30.0)

    # Equipment age degrades reliability (nonlinear: diminishing returns after 30yr)
    risk += 15.0 * (1 - np.exp(-equipment_age / 15.0))

    # Extreme temperatures increase risk
    temp_stress = np.where(temperature < -5, (-5 - temperature) * 0.4, 0)
    temp_stress += np.where(temperature > 35, (temperature - 35) * 0.5, 0)
    risk += temp_stress

    # High wind speed
    risk += np.where(wind_speed > 40, (wind_speed - 40) * 0.3, 0)
    risk += np.where(wind_speed > 70, (wind_speed - 70) * 0.5, 0)

    # Heavy precipitation
    risk += np.where(precipitation > 20, (precipitation - 20) * 0.2, 0)

    # Storm and snow conditions add flat penalty
    risk += np.where(weather_cond == 4, 12.0, 0)  # STORM
    risk += np.where(weather_cond == 3, 5.0, 0)   # SNOW
    risk += np.where(weather_cond == 2, 3.0, 0)   # RAINY

    # Interaction: old equipment in bad weather is worse
    risk += np.where(
        (equipment_age > 20) & (weather_cond >= 3),
        equipment_age * 0.3,
        0,
    )

    # Interaction: high criticality + many faults is dangerous
    risk += np.where(
        (criticality >= 4) & (recent_faults >= 5),
        10.0,
        0,
    )

    # Add realistic noise
    noise = rng.normal(0, 2.5, size=n_samples)
    risk += noise

    # Clip to 0-100
    risk = np.clip(risk, 0, 100)

    return X, risk


def train_model():
    """Train the risk prediction model and save it to disk."""
    print("Generating synthetic training data...")
    X, y = generate_synthetic_data(n_samples=8000)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    print(f"Training set: {X_train.shape[0]} samples")
    print(f"Test set: {X_test.shape[0]} samples")

    model = GradientBoostingRegressor(
        n_estimators=200,
        max_depth=5,
        learning_rate=0.1,
        min_samples_leaf=10,
        subsample=0.8,
        random_state=42,
    )

    print("Training GradientBoostingRegressor...")
    model.fit(X_train, y_train)

    y_pred = model.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    print(f"Test MAE: {mae:.2f}")
    print(f"Test R2:  {r2:.4f}")

    importances = model.feature_importances_
    print("\nFeature importances:")
    for name, imp in sorted(zip(FEATURE_NAMES, importances), key=lambda x: -x[1]):
        print(f"  {name}: {imp:.4f}")

    joblib.dump(model, MODEL_PATH)
    print(f"\nModel saved to {MODEL_PATH}")

    return model


if __name__ == "__main__":
    train_model()
