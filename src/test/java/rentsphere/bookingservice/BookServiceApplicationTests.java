package rentsphere.bookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingStatus;
import rentsphere.bookingservice.booking.web.BookingRequest;
import rentsphere.bookingservice.house.House;
import rentsphere.bookingservice.house.HouseClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres"));

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
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
    }

    @Test
    void whenGetBookingsThenReturn() {
        String code = "123456789";
        House house = House.of(code, "Acme Fresh Start Housing",
                "Chicago", "IL", "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                4, true, true);
        given(houseClient.getHouseByCode(code)).willReturn(Mono.just(house));
        BookingRequest bookingRequest = new BookingRequest(code, 1);
        Booking expectedBooking = webTestClient.post().uri("/bookings")
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();
        assertThat(expectedBooking).isNotNull();

        webTestClient.get().uri("/bookings")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Booking.class).value(orders -> {
                    assertThat(orders.stream().filter(booking -> booking.houseCode().equals(code)).findAny()).isNotEmpty();
                });
    }

    @Test
    void whenPostRequestAndBookExistsThenOrderAccepted() {
        String code = "123456789";
        House house = House.of(code, "Acme Fresh Start Housing",
                "Chicago", "IL", "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                4, true, true);
        given(houseClient.getHouseByCode(code)).willReturn(Mono.just(house));
        BookingRequest bookingRequest = new BookingRequest(code, 3);

        Booking createdBooking = webTestClient.post().uri("/bookings")
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();

        assertThat(createdBooking).isNotNull();
        assertThat(createdBooking.houseCode()).isEqualTo(bookingRequest.code());
        assertThat(createdBooking.quantity()).isEqualTo(bookingRequest.quantity());
        assertThat(createdBooking.status()).isEqualTo(BookingStatus.ACCEPTED);
    }

    @Test
    void whenPostRequestAndBookNotExistsThenOrderRejected() {
        String code = "12456789";
        given(houseClient.getHouseByCode(code)).willReturn(Mono.empty());
        BookingRequest bookingRequest = new BookingRequest(code, 3);

        Booking createdBooking = webTestClient.post().uri("/bookings")
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).returnResult().getResponseBody();

        assertThat(createdBooking).isNotNull();
        assertThat(createdBooking.houseCode()).isEqualTo(bookingRequest.code());
        assertThat(createdBooking.quantity()).isEqualTo(bookingRequest.quantity());
        assertThat(createdBooking.status()).isEqualTo(BookingStatus.REJECTED);
    }

}
