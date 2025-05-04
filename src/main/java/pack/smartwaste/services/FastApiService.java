package pack.smartwaste.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pack.smartwaste.RequestsEntities.ImageRequest;
import pack.smartwaste.RequestsEntities.FastApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.smartwaste.RequestsEntities.TextRequest;

@Service
public class FastApiService {

    private static final Logger logger = LoggerFactory.getLogger(FastApiService.class);

    private final String FAST_API_URL = "http://localhost:8000/";
    private final String IMAGE = "similar-image/";
    private final String TEXT = "similar-text/";

    public FastApiResponse getSimilarImages(String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();

        ImageRequest request = new ImageRequest(imageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ImageRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending request to FastAPI: {}", request);

        ResponseEntity<FastApiResponse> response = restTemplate.postForEntity(
                FAST_API_URL + IMAGE,
                entity,
                FastApiResponse.class
        );

        return response.getBody();
    }
    public FastApiResponse getSimilarImagesByText(String text) {
        RestTemplate restTemplate = new RestTemplate();

        TextRequest request = new TextRequest(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TextRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending request to FastAPI: {}", request);

        ResponseEntity<FastApiResponse> response = restTemplate.postForEntity(
                FAST_API_URL + TEXT,
                entity,
                FastApiResponse.class
        );

        return response.getBody();
    }

}