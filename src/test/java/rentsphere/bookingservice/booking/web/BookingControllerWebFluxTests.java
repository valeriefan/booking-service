package rentsphere.bookingservice.booking.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingService;
import rentsphere.bookingservice.booking.domain.BookingStatus;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(BookingController.class)
class BookingControllerWebFluxTests {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean @Autowired
    private BookingService bookingService;

    @Test
    void whenHouseNotAvailableThenRejectBooking() {
        var bookingRequest = new BookingRequest("123456789", 5);
        var expectedBooking = BookingService.buildRejectedBooking(
                bookingRequest.code(), bookingRequest.quantity());
        given(bookingService.submitBooking(
                bookingRequest.code(), bookingRequest.quantity())
        ).willReturn(Mono.just(expectedBooking));

        webClient
                .post()
                .uri("/bookings")
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Booking.class).value(actualOrder -> {
                    assertThat(actualOrder).isNotNull();
                    assertThat(actualOrder.status()).isEqualTo(BookingStatus.REJECTED);
                });
    }
}
