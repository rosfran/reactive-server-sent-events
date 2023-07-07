package org.streamer.harvester.config;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


@Configuration
public class WebClientConfig {

    @Value("${api.videostreaming.url}")
    private String baseUrl;
    @Value("${api.timeout}")
    private int timeout;

    @Bean
    public WebClient webClientWithTimeout() {
        final var httpClient = HttpClient.create().
                followRedirect(true).noSSL().
                option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder().filter(ExchangeFilterFunctions
                .basicAuthentication("sytac", "4p9g-Dv7T-u8fe-iz6y-SRW2")).baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }
}