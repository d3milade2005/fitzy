package com.fashion_app.closet_api.service;


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

            // In a real app, you would parse the JSON response.
            // For brevity, we return a mocked string if the call succeeds.
            String response = restTemplate.getForObject(url, String.class);

            // TODO: Parse 'response' using Jackson/Gson to get description
            // For now, let's assume if it didn't throw an error, it's sunny.
            return "Sunny, 25°C";

        } catch (Exception e) {
            log.error("Weather API failed for {}: {}", location, e.getMessage());
            return "Weather unavailable"; // Fail gracefully
        }
    }
}