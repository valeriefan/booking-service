package rentsphere.bookingservice.booking.domain;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("bookings")
public record Booking (

        @Id
        Long id,

        String houseCode,
        String houseName,
        String houseCity,
        String houseState,
        String housePhoto,
        int quantity,
        boolean wifi,
        boolean laundry,
        BookingStatus status,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @CreatedBy
        String createdBy,

        @LastModifiedBy
        String lastModifiedBy,

        @Version
        int version
) {
    public static Booking of(
            String code, String name, String city, String state, String photo,
            int quantity, boolean wifi, boolean laundry, BookingStatus status
    ) {
        return new Booking(
                null, code, name, city, state, photo,
                quantity, wifi, laundry, status, null, null,
                null, null, 0
        );
    }
}
