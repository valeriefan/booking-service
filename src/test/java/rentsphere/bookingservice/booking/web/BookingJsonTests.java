package rentsphere.bookingservice.booking.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingJsonTests {

    @Autowired
    private JacksonTester<Booking> json;

    @Test
    void testSerialize() throws Exception {
        var booking = new Booking(394L, "123456789", "Acme Fresh Start Housing",
                "Chicago", "IL",
                "https://angular.dev/assets/images/tutorials/common/bernard-hermant-CLKGGwIBTaY-unsplash.jpg",
                1, true, true, BookingStatus.ACCEPTED, Instant.now(), Instant.now(),
                "jon", "marlena", 21);
        var jsonContent = json.write(booking);
        assertThat(jsonContent).extractingJsonPathNumberValue("@.id")
                .isEqualTo(booking.id().intValue());
        assertThat(jsonContent).extractingJsonPathStringValue("@.houseCode")
                .isEqualTo(booking.houseCode());
        assertThat(jsonContent).extractingJsonPathStringValue("@.houseCity")
                .isEqualTo(booking.houseCity());
        assertThat(jsonContent).extractingJsonPathStringValue("@.houseState")
                .isEqualTo(booking.houseState());
        assertThat(jsonContent).extractingJsonPathStringValue("@.housePhoto")
                .isEqualTo(booking.housePhoto());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.quantity")
                .isEqualTo(booking.quantity());
        assertThat(jsonContent).extractingJsonPathStringValue("@.status")
                .isEqualTo(booking.status().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.createdDate")
                .isEqualTo(booking.createdDate().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedDate")
                .isEqualTo(booking.lastModifiedDate().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.createdBy")
                .isEqualTo(booking.createdBy());
        assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedBy")
                .isEqualTo(booking.lastModifiedBy());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.version")
                .isEqualTo(booking.version());
    }

}
