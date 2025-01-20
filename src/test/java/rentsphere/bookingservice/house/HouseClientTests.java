package rentsphere.bookingservice.house;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class HouseClientTests {
    private MockWebServer mockWebServer;
    private HouseClient houseClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.houseClient = new HouseClient(webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void whenHouseExistsThenReturnHouse() {
        var code = "123456789";

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                        "code": %s,
                        "name": "Acme Fresh Start Housing",\s
                        "city": "Chicago",\s
                        "state": "IL",
                        "photo": "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                        "availableUnits": 4,
                        "wifi": true,
                        "laundry": true
                        }""".formatted(code));

        mockWebServer.enqueue(mockResponse);

        Mono<House> book = houseClient.getHouseByCode(code);

        StepVerifier.create(book)
                .expectNextMatches(
                        h -> h.code().equals(code))
                .verifyComplete();
    }

    @Test
    void whenHouseNotExistsThenReturnEmpty() {
        var code = "123456789";

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404);

        mockWebServer.enqueue(mockResponse);

        StepVerifier.create(houseClient.getHouseByCode(code))
                .expectNextCount(0)
                .verifyComplete();
    }
}
