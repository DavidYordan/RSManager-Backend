package com.rsmanager.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmanager.model.UsdRate;
import com.rsmanager.repository.local.UsdRateRepository;
import com.rsmanager.service.UsdRateService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

@Service
public class UsdRateServiceImpl implements UsdRateService {

    private final UsdRateRepository usdRateRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${usdrate.url}")
    private String usdrateUrl;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UsdRateServiceImpl(UsdRateRepository usdRateRepository) {
        this.usdRateRepository = usdRateRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) 
    public void fetchAndStoreRates() {
        LocalDate latestDate = usdRateRepository.findLatestDate().orElse(LocalDate.of(2023, 1, 1));
        LocalDate today = LocalDate.now();

        if (!latestDate.isBefore(today)) {
            fetchAndStoreRate(today.minusDays(1));
            fetchAndStoreRate(today);
            return;
        }

        LocalDate startDate = latestDate.plusDays(1);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(today)) {
            fetchAndStoreRate(currentDate);
            currentDate = currentDate.plusDays(1);
        }
    }

    private void fetchAndStoreRate(LocalDate date) {
        try {
            String url = usdrateUrl.replace("{date}", date.format(formatter));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode usdNode = root.path("usd");

                if (usdNode.isMissingNode()) {
                    // 没有找到 "usd" 节点，跳过
                    return;
                }

                Iterator<Map.Entry<String, JsonNode>> fields = usdNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String currency = entry.getKey();
                    Double rate = entry.getValue().asDouble();

                    // 检查是否已经存在该货币和日期的数据
                    if (!usdRateRepository.existsByCurrencyAndDate(currency, date)) {
                        UsdRate usdRate = UsdRate.builder()
                                .currency(currency)
                                .rate(rate)
                                .date(date)
                                .build();
                        usdRateRepository.save(usdRate);
                    }
                }
            }
        } catch (Exception e) {
            // 处理异常，例如 404 错误等，忽略该日期
            System.err.println("Failed to fetch data for date " + date + ": " + e.getMessage());
        }
    }
}