package com.catalyst.ProCounsellor.processor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.catalyst.ProCounsellor.processor.dto.CollegeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CollegeService {
	private static final Logger logger = LoggerFactory.getLogger(CollegeService.class);

    private final RestTemplate restTemplate;

    public CollegeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CollegeResponse getCollegeInfo(String name) {
        String url = "https://procounsel.co.in/api/college/?name=" + name.replace(" ", "+");

        logger.info("Fetching college info for: {}", name);
        logger.debug("Calling URL: {}", url);

        try {
            CollegeResponse response = restTemplate.getForObject(url, CollegeResponse.class);
            logger.info("Successfully fetched data for: {}", name);
            return response;
        } catch (Exception ex) {
            logger.error("Error fetching college data for: {} - {}", name, ex.getMessage(), ex);
            throw ex;
        }
    }
}
