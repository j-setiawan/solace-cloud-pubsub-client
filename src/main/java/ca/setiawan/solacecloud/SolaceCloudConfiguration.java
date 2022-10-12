package ca.setiawan.solacecloud;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.solace.messaging.MessagingService;
import com.solace.messaging.config.AuthenticationStrategy;
import com.solace.messaging.config.profile.ConfigurationProfile;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        DocumentContext jsonContext = JsonPath.parse(response.body());

        // Doesn't matter which protocol
        Map<String, Object> credentials = jsonContext.read("data.messagingProtocols.[0]");
        List<String> smfUris = jsonContext.read("data.messagingProtocols.[?(@.name==\"SMF\")].endPoints.[?(@.name==\"Secured SMF\")].uris.[0]");

        String username = (String) credentials.get("username");
        String password = (String) credentials.get("password");
        String url = smfUris.get(0);

        Properties properties = new Properties();
        properties.setProperty("host", url);

        MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(properties)
                .withAuthenticationStrategy(AuthenticationStrategy.BasicUserNamePassword.of(username, password))
                .build().connect();

        return messagingService;
    }

}
