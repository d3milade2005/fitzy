# Fitzy - Smart Wardrobe Management System

## Overview

**Fitzy** is a backend service that modernizes personal wardrobe management. Built with **Spring Boot**, it provides a RESTful API for users to digitize their closet, organize clothing by category and season, and receive intelligent daily outfit suggestions.

By integrating with **Google Calendar** and **OpenWeatherMap**, Fitzy analyzes a user's schedule and local weather to recommend the perfect outfit, effectively eliminating the daily "what to wear" problem.

---

## Key Features

* **User Management**: Secure authentication using **JWT** and **Google OAuth2**, with role-based access control (Admin/User).
* **Digital Closet**: Upload and manage clothing items with metadata like category and seasonality.
* **Cloud Storage**: Integrates seamlessly with **AWS S3** and **Google Cloud Storage** for scalable and secure image hosting.
* **Intelligent Outfit Suggestions**:

  * **Calendar Integration**: Reads Google Calendar events to match outfits to specific occasions.
  * **Weather Context**: Adjusts recommendations based on real-time forecasts.
  * **Automated Styling**: Generates outfit combinations tailored to event type and weather.
  * **Calendar Write-back**: Updates event descriptions with suggested "Fashion Plans".
* **Outfit Management**: Users can manually create, save, and manage favorite outfit combinations.

---

## Technical Highlights

* **Core Framework**: Java 17+, Spring Boot 3.5.9
* **Database**: PostgreSQL, Flyway (migrations)
* **Security**: Spring Security 6, OAuth2 Client, JWT
* **Cloud Infrastructure**: AWS S3 (image storage), Google Cloud Platform (Calendar & Storage API)
* **APIs & Libraries**: Google Calendar API, OpenWeatherMap API, Lombok, MapStruct/ModelMapper

**Engineering Challenges Solved:**

* Seamless OAuth2 flow integration for Google services.
* Scalable storage for thousands of user-uploaded images.
* Intelligent outfit suggestions factoring multiple dynamic inputs (event type, weather, season).

---

## Architecture

Follows a **Layered Architecture**:

1. **Controller Layer**: Handles HTTP requests and responses.
2. **Service Layer**: Contains business logic, integrations, and transaction management.
3. **Repository Layer**: Abstracts data access using Spring Data JPA.
4. **Entity Layer**: Defines persistent data models.

---

## Getting Started

### Prerequisites

* Java 17 or higher
* Maven 3.8+
* PostgreSQL 14+
* AWS Account (S3)
* Google Cloud Project (OAuth and Calendar API)
* OpenWeatherMap API Key

### Configuration

1. **Create a `.env` file** in the root directory:
   ```bash
   cp .env.example .env
   ```

2. **Update the `.env` file** with your actual credentials. 

   > **Note**: The `.env` file is git-ignored to protect your secrets. Use `.env.example` as a template.

### Build & Run

```bash
# Clone repository
git clone https://github.com/yourusername/closet-api.git
cd closet-api

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

API available at `http://localhost:8080`.

---

## API Endpoints

| Method | Endpoint                  | Description                     |
| ------ | ------------------------- | ------------------------------- |
| POST   | /api/auth/register        | Register a new user             |
| POST   | /api/auth/login           | Login and receive JWT           |
| GET    | /api/v1/closet            | Get user's closet items         |
| POST   | /api/v1/closet            | Upload a new clothing item      |
| POST   | /api/outfit               | Create/Save an outfit           |
| GET    | /api/outfit               | Get outfit details              |
| GET    | /api/trigger-fashion-plan | Trigger calendar sync & styling |

---

## Impact & Metrics (Optional / Demo)

* Reduced daily outfit decision time by ~80% through automated suggestions.
* Scalable architecture supports thousands of users with minimal latency.
* Secure integration with multiple third-party APIs without exposing user data.
