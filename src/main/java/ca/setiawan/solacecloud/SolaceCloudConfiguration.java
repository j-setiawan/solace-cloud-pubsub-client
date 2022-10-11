package ca.setiawan.solacecloud;

import com.solace.messaging.MessagingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Configuration
@ConditionalOnProperty(value = "solacecloud.api-key")
@EnableConfigurationProperties(SolaceCloudProperties.class)
public class SolaceCloudConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public MessagingService messagingService(SolaceCloudProperties solaceCloudProperties) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.solace.cloud/api/v0/services/" + solaceCloudProperties.getServiceId()))
                .header("Authorization", "Bearer " + solaceCloudProperties.getApiKey())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return null;
    }

}
