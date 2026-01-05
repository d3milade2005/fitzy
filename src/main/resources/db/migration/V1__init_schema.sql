CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    google_access_token TEXT,
    google_refresh_token TEXT,
    google_token_expiry TIMESTAMP,
    verification_code VARCHAR(255),
    verification_code_expiry TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    body_shape_image_key VARCHAR(500) NOT NULL,
    style_preferences JSONB
);

CREATE TABLE closet_items (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    image_key VARCHAR(500) NOT NULL,
    category VARCHAR(50),
    season VARCHAR(50),
    ai_description TEXT,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_closet_items_user_id ON closet_items(user_id);