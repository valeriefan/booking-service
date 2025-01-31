package rentsphere.bookingservice;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingStatus;
import rentsphere.bookingservice.booking.event.BookingAcceptedMessage;
import rentsphere.bookingservice.booking.web.BookingRequest;
import rentsphere.bookingservice.house.House;
import rentsphere.bookingservice.house.HouseClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestChannelBinderConfiguration.class)
@Testcontainers
class BookServiceApplicationTests {

    // Customer
    private static KeycloakToken bjornTokens;
    // Customer and employee
    private static KeycloakToken isabelleTokens;

    @Container
    private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak")
            .withRealmImportFile("/test-realm-config.json");

    @Container
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres"));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutputDestination output;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private HouseClient houseClient;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", BookServiceApplicationTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/RentSphere");
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
    }

    @BeforeAll
    static void generateAccessTokens() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakContainer.getAuthServerUrl() + "/realms/RentSphere/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        isabelleTokens = authenticateWith("isabelle", webClient);
        bjornTokens = authenticateWith("bjorn", webClient);
    }

    @Test
    void whenGetOwnOrdersThenReturn() throws IOException {
        String code = "123456789";
        House house = House.of(code, "Acme Fresh Start Housing",
                "Chicago", "IL", "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                4, true, true);
        given(houseClient.getHouseByCode(code)).willReturn(Mono.just(house));
        BookingRequest orderRequest = new BookingRequest(code, 1);

        Booking expectedOrder = webTestClient.post().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();
        assertThat(expectedOrder).isNotNull();
        assertThat(objectMapper.readValue(output.receive().getPayload(), BookingAcceptedMessage.class))
                .isEqualTo(new BookingAcceptedMessage(expectedOrder.id()));

        webTestClient.get().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Booking.class).value(orders -> {
                    List<Long> orderIds = orders.stream()
                            .map(Booking::id)
                            .collect(Collectors.toList());
                    assertThat(orderIds).contains(expectedOrder.id());
                });
    }

    @Test
    void whenGetOrdersForAnotherUserThenNotReturned() throws IOException {
        String code = "123456789";
        House house = House.of(code, "Acme Fresh Start Housing",
                "Chicago", "IL", "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                4, true, true);
        given(houseClient.getHouseByCode(code)).willReturn(Mono.just(house));
        BookingRequest orderRequest = new BookingRequest(code, 1);

        Booking orderByBjorn = webTestClient.post().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();
        assertThat(orderByBjorn).isNotNull();
        assertThat(objectMapper.readValue(output.receive().getPayload(), BookingAcceptedMessage.class))
                .isEqualTo(new BookingAcceptedMessage(orderByBjorn.id()));

        Booking orderByIsabelle = webTestClient.post().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();
        assertThat(orderByIsabelle).isNotNull();
        assertThat(objectMapper.readValue(output.receive().getPayload(), BookingAcceptedMessage.class))
                .isEqualTo(new BookingAcceptedMessage(orderByIsabelle.id()));

        webTestClient.get().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Booking.class)
                .value(orders -> {
                    List<Long> orderIds = orders.stream()
                            .map(Booking::id)
                            .collect(Collectors.toList());
                    assertThat(orderIds).contains(orderByBjorn.id());
                    assertThat(orderIds).doesNotContain(orderByIsabelle.id());
                });
    }

    @Test
    void whenPostRequestAndBookExistsThenOrderAccepted() throws IOException {
        String code = "123456789";
        House house = House.of(code, "Acme Fresh Start Housing",
                "Chicago", "IL", "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                4, true, true);
        given(houseClient.getHouseByCode(code)).willReturn(Mono.just(house));
        BookingRequest orderRequest = new BookingRequest(code, 3);

        Booking createdOrder = webTestClient.post().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class)
                .value(order -> {
                    assertThat(order.houseCode()).isEqualTo(orderRequest.code());
                    assertThat(order.quantity()).isEqualTo(orderRequest.quantity());
                    assertThat(order.houseName()).isEqualTo(house.name());
                    assertThat(order.status()).isEqualTo(BookingStatus.ACCEPTED);
                })
                .returnResult().getResponseBody();

        assert createdOrder != null;
        assertThat(objectMapper.readValue(output.receive().getPayload(), BookingAcceptedMessage.class))
                .isEqualTo(new BookingAcceptedMessage(createdOrder.id()));
    }

    @Test
    void whenPostRequestAndBookNotExistsThenOrderRejected() {
        String code = "123456789";
        given(houseClient.getHouseByCode(code)).willReturn(Mono.empty());
        BookingRequest orderRequest = new BookingRequest(code, 3);

        webTestClient.post().uri("/bookings")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class)
                .value(order -> {
                    assertThat(order.houseCode()).isEqualTo(orderRequest.code());
                    assertThat(order.quantity()).isEqualTo(orderRequest.quantity());
                    assertThat(order.status()).isEqualTo(BookingStatus.REJECTED);
                });
    }

    private static KeycloakToken authenticateWith(String username, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "rentsphere-test")
                        .with("username", username)
                        .with("password", "password")
                )
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    private record KeycloakToken(String accessToken) {

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }

    }

}
