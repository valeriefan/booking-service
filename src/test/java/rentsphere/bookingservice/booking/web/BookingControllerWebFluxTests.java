package rentsphere.bookingservice.booking.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingService;
import rentsphere.bookingservice.booking.domain.BookingStatus;
import rentsphere.bookingservice.config.SecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerWebFluxTests {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean @Autowired
    private BookingService bookingService;

    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = new BookingRequest("1234567890", 3);
        var expectedOrder = BookingService.buildRejectedBooking(orderRequest.code(), orderRequest.quantity());
        given(bookingService.submitBooking(orderRequest.code(), orderRequest.quantity()))
                .willReturn(Mono.just(expectedOrder));

        webClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_customer")))
                .post()
                .uri("/bookings")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).value(actualOrder -> {
                    assertThat(actualOrder).isNotNull();
                    assertThat(actualOrder.status()).isEqualTo(BookingStatus.REJECTED);
                });
    }
}
