package rentsphere.bookingservice.house;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class HouseClient {
    private static final String HOUSES_ROOT_API = "/houses/";
    private final WebClient webClient;

    public HouseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<House> getHouseByCode(String code) {
        return webClient
                .get()
                .uri(HOUSES_ROOT_API + code)
                .retrieve()
                .bodyToMono(House.class)
                .timeout(Duration.ofSeconds(3), Mono.empty())
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(Exception.class,
                        exception -> Mono.empty());
    }
}
