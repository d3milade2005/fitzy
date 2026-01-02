CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    is_profile_complete BOOLEAN NOT NULL DEFAULT FALSE,
    location_city VARCHAR(255),
    preferences JSONB,
    google_refresh_token TEXT,
    verification_code VARCHAR(255),
    verification_code_expiry TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);