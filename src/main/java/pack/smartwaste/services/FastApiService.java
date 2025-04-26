package pack.smartwaste.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pack.smartwaste.RequestsEntities.ImageRequest;
import pack.smartwaste.RequestsEntities.ImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FastApiService {

    private static final Logger logger = LoggerFactory.getLogger(FastApiService.class);

    private final String FAST_API_URL = "http://localhost:8000/similar/";

    public ImageResponse getSimilarImages(String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();

        ImageRequest request = new ImageRequest(imageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ImageRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending request to FastAPI: {}", request);

        ResponseEntity<ImageResponse> response = restTemplate.postForEntity(
                FAST_API_URL,
                entity,
                ImageResponse.class
        );

        return response.getBody();
    }

}