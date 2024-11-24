package com.rsmanager.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmanager.model.UsdRate;
import com.rsmanager.repository.local.RegionCurrencyRepository;
import com.rsmanager.repository.local.UsdRateRepository;
import com.rsmanager.service.UsdRateService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsdRateServiceImpl implements UsdRateService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final RegionCurrencyRepository regionCurrencyRepository;
    private final UsdRateRepository usdRateRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${usdrate.url}")
    private String usdrateUrl;

    @Value("${fetch.usdrate}")
    private boolean canFetchUsdRate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) 
    public void fetchAndStoreRates() {

        if (!canFetchUsdRate) {
            logger.info("Fetching USD rate is disabled.");
            return;
        }

        List<String> currencies = regionCurrencyRepository.findAllCurrencyCodes();
            if (currencies.isEmpty()) {
                logger.warn("No currencies found to fetch rates.");
                return;
            }

        String currencyCodes = String.join(",", currencies);

        LocalDate latestDate = usdRateRepository.findLatestDate().orElse(LocalDate.of(2023, 1, 1));
        LocalDate today = LocalDate.now();

        if (!latestDate.isBefore(today)) {
            fetchAndStoreRate(today.minusDays(1), currencyCodes);
            fetchAndStoreRate(today, currencyCodes);
            return;
        }

        LocalDate startDate = latestDate.plusDays(1);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(today)) {
            fetchAndStoreRate(currentDate, currencyCodes);
            currentDate = currentDate.plusDays(1);
        }
    }

    private void fetchAndStoreRate(LocalDate date, String currencyCodes) {
        try {
            String url = usdrateUrl.replace("{currencies}", currencyCodes).replace("{date}", date.format(formatter));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.path("data").path(date.format(formatter));

                if (dataNode.isMissingNode()) {
                    logger.warn("No data found for date: " + date);
                    return;
                }

                Iterator<Map.Entry<String, JsonNode>> fields = dataNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String currencyCode = entry.getKey();
                    Double rate = entry.getValue().asDouble();

                    UsdRate usdRate = UsdRate.builder()
                            .currencyCode(currencyCode)
                            .rate(rate)
                            .date(date)
                            .build();
                    usdRateRepository.save(usdRate);
                }
            }
        } catch (Exception e) {
            // 处理异常，例如 404 错误等，忽略该日期
            logger.error("Failed to fetch data for date " + date + ": " + e.getMessage());
        }
    }
}