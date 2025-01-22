package rentsphere.bookingservice.booking.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestJsonTests {

    @Autowired
    private JacksonTester<BookingRequest> json;

    @Test
    void testDeserialize() throws Exception {
        var content = """
                {
                    "code": "123456789",
                    "quantity": 1
                }
                """;
        assertThat(this.json.parse(content))
                .usingRecursiveComparison().isEqualTo(new BookingRequest("123456789", 1));
    }

}
