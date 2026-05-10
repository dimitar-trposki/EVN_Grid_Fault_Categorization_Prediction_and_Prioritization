-- ROLES
CREATE TABLE IF NOT EXISTS role (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

-- USERS
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20),
    role_id BIGINT REFERENCES role(id)
    );

-- CUSTOMERS
CREATE TABLE IF NOT EXISTS customer (
                                        id BIGSERIAL PRIMARY KEY,
                                        first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20)
    );

-- REGIONS
CREATE TABLE IF NOT EXISTS region (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

-- LOCATIONS
CREATE TABLE IF NOT EXISTS location (
                                        id BIGSERIAL PRIMARY KEY,
                                        region_id BIGINT NOT NULL REFERENCES region(id),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address VARCHAR(255) NOT NULL,
    municipality VARCHAR(100),
    city_area VARCHAR(100),
    street VARCHAR(150),
    criticality_level VARCHAR(20),
    location_type VARCHAR(50)
    );

-- EQUIPMENT
CREATE TABLE IF NOT EXISTS equipment (
                                         id BIGSERIAL PRIMARY KEY,
                                         asset_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    equipment_type VARCHAR(30) NOT NULL,
    status VARCHAR(20),
    installation_year INTEGER,
    age_years INTEGER,
    last_maintenance_date DATE,
    location_id BIGINT NOT NULL REFERENCES location(id)
    );

-- WEATHER DATA
CREATE TABLE IF NOT EXISTS weather_data (
                                            id BIGSERIAL PRIMARY KEY,
                                            location_id BIGINT NOT NULL REFERENCES location(id),
    recorded_at TIMESTAMP NOT NULL,
    temperature DOUBLE PRECISION,
    wind_speed DOUBLE PRECISION,
    humidity DOUBLE PRECISION,
    precipitation DOUBLE PRECISION,
    weather_condition VARCHAR(30),
    source_api VARCHAR(100)
    );

-- FAULT REPORTS
CREATE TABLE IF NOT EXISTS fault_report (
                                            id BIGSERIAL PRIMARY KEY,
                                            report_code VARCHAR(50) NOT NULL UNIQUE,
    tracking_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT REFERENCES customer(id),
    reported_by_user_id BIGINT REFERENCES users(id),
    location_id BIGINT REFERENCES location(id),
    equipment_id BIGINT REFERENCES equipment(id),
    fault_type_id BIGINT,
    source_type VARCHAR(30),
    raw_description TEXT,
    reported_at TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    customer_visible_status VARCHAR(30),
    submitted_by_customer BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    safety_risk BOOLEAN DEFAULT FALSE,
    affected_users_estimate INTEGER,
    outage_scope VARCHAR(50)
    );

-- FAULT STATUS HISTORY
CREATE TABLE IF NOT EXISTS fault_status_history (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    fault_report_id BIGINT NOT NULL REFERENCES fault_report(id),
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by_user_id BIGINT REFERENCES users(id),
    changed_by_customer_id BIGINT REFERENCES customer(id),
    changed_at TIMESTAMP NOT NULL,
    note TEXT,
    customer_visible BOOLEAN DEFAULT FALSE
    );

-- ATTACHMENTS
CREATE TABLE IF NOT EXISTS attachment (
                                          id BIGSERIAL PRIMARY KEY,
                                          fault_report_id BIGINT NOT NULL REFERENCES fault_report(id),
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    uploaded_by_user_id BIGINT REFERENCES users(id),
    uploaded_by_customer_id BIGINT REFERENCES customer(id),
    uploaded_at TIMESTAMP NOT NULL
    );

-- CREWS
CREATE TABLE IF NOT EXISTS crew (
                                    id BIGSERIAL PRIMARY KEY,
                                    crew_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20),
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION
    );

-- CREW MEMBERS
CREATE TABLE IF NOT EXISTS crew_member (
                                           id BIGSERIAL PRIMARY KEY,
                                           crew_id BIGINT NOT NULL REFERENCES crew(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100),
    position VARCHAR(50),
    assigned_at TIMESTAMP
    );

-- FAULT ASSIGNMENTS
CREATE TABLE IF NOT EXISTS fault_assignment (
                                                id BIGSERIAL PRIMARY KEY,
                                                fault_report_id BIGINT NOT NULL REFERENCES fault_report(id),
    crew_id BIGINT NOT NULL REFERENCES crew(id),
    assigned_by_user_id BIGINT REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    completed_at TIMESTAMP,
    assignment_status VARCHAR(30),
    assignment_note TEXT,
    location_id BIGINT REFERENCES location(id)
    );

-- INTERVENTIONS
CREATE TABLE IF NOT EXISTS intervention (
                                            id BIGSERIAL PRIMARY KEY,
                                            fault_report_id BIGINT NOT NULL REFERENCES fault_report(id),
    crew_id BIGINT NOT NULL REFERENCES crew(id),
    location_id BIGINT REFERENCES location(id),
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    duration_minutes INTEGER,
    resolution_status VARCHAR(30),
    resolution_notes TEXT,
    root_cause TEXT
    );

-- RISK PREDICTIONS
CREATE TABLE IF NOT EXISTS risk_prediction (
                                               id BIGSERIAL PRIMARY KEY,
                                               location_id BIGINT REFERENCES location(id),
    equipment_id BIGINT REFERENCES equipment(id),
    risk_score DOUBLE PRECISION,
    risk_level VARCHAR(20),
    contributing_factors TEXT,
    prediction_date TIMESTAMP
    );

-- SYSTEM NOTIFICATIONS
CREATE TABLE IF NOT EXISTS system_notification (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   user_id BIGINT REFERENCES users(id),
    customer_id BIGINT REFERENCES customer(id),
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(50),
    channel VARCHAR(30),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
    );

-- AUDIT LOGS
CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT REFERENCES users(id),
    entity_name VARCHAR(100),
    entity_id BIGINT,
    action_type VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL
    );

-- IMPORT BATCHES
CREATE TABLE IF NOT EXISTS import_batch (
                                            id BIGSERIAL PRIMARY KEY,
                                            created_by_user_id BIGINT REFERENCES users(id),
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    total_records INTEGER,
    successful_records INTEGER,
    failed_records INTEGER,
    import_status VARCHAR(30),
    created_at TIMESTAMP NOT NULL
    );

-- EXPORT BATCHES
CREATE TABLE IF NOT EXISTS export_batch (
                                            id BIGSERIAL PRIMARY KEY,
                                            created_by_user_id BIGINT REFERENCES users(id),
    export_type VARCHAR(50),
    file_name VARCHAR(255),
    file_format VARCHAR(20),
    status VARCHAR(30),
    created_at TIMESTAMP NOT NULL
    );

-- DEFAULT ROLES
INSERT INTO role (name, description) VALUES
                                         ('ADMIN', 'System Administrator'),
                                         ('OPERATOR', 'Call Center Operator'),
                                         ('DISPATCHER', 'Fault Dispatcher'),
                                         ('MANAGER', 'Manager'),
                                         ('FIELD_CREW', 'Field Crew Member')
    ON CONFLICT (name) DO NOTHING;