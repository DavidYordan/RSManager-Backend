package com.rsmanager.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmanager.model.UsdRate;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsdRateServiceImpl implements UsdRateService {

    private static final Logger logger = LoggerFactory.getLogger(UsdRateServiceImpl.class);

    private final UsdRateRepository usdRateRepository;
    private final RestTemplate restTemplate;

    @Value("${usdrate.url}")
    private String usdrateUrl;

    @Value("${usdrate.latest.url}")
    private String usdrateLatestUrl;

    @Value("${fetch.usdrate}")
    private boolean canFetchUsdRate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LocalDate specialLocalDate = LocalDate.of(1970, 1, 1);

    @Override
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void fetchAndStoreRates() {

        if (!canFetchUsdRate) {
            logger.info("Fetching USD rate is disabled.");
            return;
        }

        LocalDate latestDate = usdRateRepository.findLatestDate().orElse(LocalDate.of(2023, 1, 1));
        LocalDate endDate = LocalDate.now();

        LocalDate currentDate = latestDate;

        while (currentDate.isBefore(endDate)) {
            fetchAndStoreRate(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        fetchAndStoreLatest();
    }

    private void fetchAndStoreRate(LocalDate date) {
        try {
            String url = usdrateUrl.replace("{date}", date.format(formatter));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper localObjectMapper = createLocalObjectMapper();
                JsonNode root = localObjectMapper.readTree(response.getBody());
                JsonNode dataNode = root.path("rates");

                if (dataNode.isMissingNode()) {
                    logger.warn("No data found for date: " + date);
                    return;
                }

                // 收集所有的 currencyCode
                Set<String> currencyCodes = new HashSet<>();
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = dataNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                    currencyCodes.add(entry.getKey());
                }

                // 查询数据库中已有的记录
                List<UsdRate> existingUsdRates = usdRateRepository.findByDateAndCurrencyCodeIn(date, currencyCodes);
                Map<String, UsdRate> existingUsdRateMap = existingUsdRates.stream()
                        .collect(Collectors.toMap(UsdRate::getCurrencyCode, Function.identity()));

                // 准备需要保存的 UsdRate 列表
                List<UsdRate> usdRatesToSave = new ArrayList<>();

                fieldsIterator = dataNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                    String currencyCode = entry.getKey();
                    Double rate = entry.getValue().asDouble();

                    UsdRate usdRate = existingUsdRateMap.get(currencyCode);
                    if (usdRate != null) {
                        // 更新已有记录的 rate
                        usdRate.setRate(rate);
                    } else {
                        // 创建新的 UsdRate 对象
                        usdRate = UsdRate.builder()
                                .currencyCode(currencyCode)
                                .rate(rate)
                                .date(date)
                                .build();
                    }
                    usdRatesToSave.add(usdRate);
                }

                // 批量保存
                usdRateRepository.saveAll(usdRatesToSave);

                logger.info("Fetched and stored data for date: " + date);
            }
        } catch (Exception e) {
            // 处理异常，例如 404 错误等，忽略该日期
            logger.error("Failed to fetch data for date " + date + ": " + e.getMessage());
        }
    }

    private void fetchAndStoreLatest() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(usdrateLatestUrl, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper localObjectMapper = createLocalObjectMapper();
                JsonNode root = localObjectMapper.readTree(response.getBody());
                JsonNode dataNode = root.path("rates");

                if (dataNode.isMissingNode()) {
                    logger.warn("No data found for latest.");
                    return;
                }

                // 收集所有的 currencyCode
                Set<String> currencyCodes = new HashSet<>();
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = dataNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                    currencyCodes.add(entry.getKey());
                }

                // 查询数据库中已有的记录
                List<UsdRate> existingUsdRates = usdRateRepository.findByDateAndCurrencyCodeIn(specialLocalDate, currencyCodes);
                Map<String, UsdRate> existingUsdRateMap = existingUsdRates.stream()
                        .collect(Collectors.toMap(UsdRate::getCurrencyCode, Function.identity()));

                // 准备需要保存的 UsdRate 列表
                List<UsdRate> usdRatesToSave = new ArrayList<>();

                fieldsIterator = dataNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                    String currencyCode = entry.getKey();
                    Double rate = entry.getValue().asDouble();

                    UsdRate usdRate = existingUsdRateMap.get(currencyCode);
                    if (usdRate != null) {
                        // 更新已有记录的 rate
                        usdRate.setRate(rate);
                    } else {
                        // 创建新的 UsdRate 对象
                        usdRate = UsdRate.builder()
                                .currencyCode(currencyCode)
                                .rate(rate)
                                .date(specialLocalDate)
                                .build();
                    }
                    usdRatesToSave.add(usdRate);
                }

                // 批量保存
                usdRateRepository.saveAll(usdRatesToSave);

                logger.info("Fetched and stored latest data.");
            }
        } catch (Exception e) {
            // 处理异常，例如 404 错误等，忽略该日期
            logger.error("Failed to fetch latest: " + e.getMessage());
        }
    }

    private ObjectMapper createLocalObjectMapper() {
        ObjectMapper localObjectMapper = new ObjectMapper();
        // 注册支持 Java 8 日期时间 API 的模块
        localObjectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return localObjectMapper;
    }
}
