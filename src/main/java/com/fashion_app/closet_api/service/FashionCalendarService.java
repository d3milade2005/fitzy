package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Repository.UserRepository;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FashionCalendarService {
    private final GoogleAuthService authService;
    private final UserRepository userRepository;
    private final WeatherService weatherService;

    @Transactional
    public void generateOutfitsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getGoogleRefreshToken() == null) {
            throw new BusinessException(
                    ErrorCode.CALENDAR_API_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Google Calendar is not connected. Please link your account first."
            );
        }

        try {
            Calendar calendarClient = authService.getCalendarService(userId);

            DateTime now = new DateTime(System.currentTimeMillis());
            DateTime nextWeek = new DateTime(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));

            log.info("Fetching events for user {} from {} to {}", userId, now, nextWeek);

            // Fetch Events
            Events events = calendarClient.events().list("primary")
                    .setTimeMin(now)
                    .setTimeMax(nextWeek)
                    .setOrderBy("startTime")
                    .setSingleEvents(true) // Expand recurring events
                    .execute();

            List<Event> items = events.getItems();

            if (items.isEmpty()) {
                log.info("No upcoming events found for user {}", userId);
                return;
            }

            int styledCount = 0;
            for (Event event : items) {
                if (processEvent(calendarClient, event)) {
                    styledCount++;
                }
            }
            log.info("Successfully styled {} events for user {}", styledCount, userId);

        } catch (Exception e) {
            log.error("Error syncing calendar for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync calendar. " + e.getMessage());
        }
    }

    private boolean processEvent(Calendar service, Event event) {
        try {
            // Idempotency Check (don't style twice). Proper implementation will be need to be done here
            if (event.getDescription() != null && event.getDescription().contains("--- FASHION PLAN ---")) {
                return false;
            }

            String location = event.getLocation();

            String weather = "No Weather";
            if (location != null && !location.isBlank()) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate(); // All-day events
                }
                long eventUnixTime = start.getValue();
                weather = weatherService.getForecast(location, eventUnixTime);
            }

            String outfitSuggestion = generateOutfit(event.getSummary(), weather);

            String originalDesc = (event.getDescription() == null) ? "" : event.getDescription();
            String newDesc = originalDesc +
                    "\n\n--- FASHION PLAN ---\n" +
                    "📍 Location: " + location + "\n" +
                    "🌤 Forecast: " + weather + "\n" +
                    "👗 Suggestion: " + outfitSuggestion;

            event.setDescription(newDesc);

            service.events().patch("primary", event.getId(), event).execute();

            return true;

        } catch (Exception e) {
            log.warn("Failed to style event '{}': {}", event.getSummary(), e.getMessage());
            return false;
        }
    }

    // This will be replaced with the ai endpoint.
    private String generateOutfit(String summary, String weather) {
        String s = summary.toLowerCase();
        String w = weather.toLowerCase();

        if (w.contains("rain")) return "Waterproof trench coat & leather boots.";
        if (s.contains("gym")) return "Activewear & running shoes.";
        if (s.contains("dinner")) return "Smart casual: Blazer & Chinos.";

        return "Comfortable casual wear.";
    }
}
