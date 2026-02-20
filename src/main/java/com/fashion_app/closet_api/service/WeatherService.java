package com.fashion_app.closet_api.service;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public String getForecast(String location, long unixTimeSeconds) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", location)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .toUriString();

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("weather") && response.has("main")) {
                JsonNode weatherArray = response.path("weather");
                String desc = weatherArray.isEmpty() ? "Clear" : weatherArray.get(0).path("description").asText("Clear");
                double temp = response.path("main").path("temp").asDouble(25.0);
                
                if (desc != null && !desc.isEmpty()) {
                    desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                }
                return String.format("%s, %.1f°C", desc, temp);
            }

            return "Sunny, 25°C";

        } catch (Exception e) {
            log.error("Weather API failed for {}: {}", location, e.getMessage());
            return "Weather unavailable";
        }
    }
}