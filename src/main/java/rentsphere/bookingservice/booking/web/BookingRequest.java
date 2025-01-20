package rentsphere.bookingservice.booking.web;

import jakarta.validation.constraints.*;

public record BookingRequest (

        @NotBlank(message = "The house reference code must be defined.")
        String code,

        @NotNull(message = "The house quantity must be defined.")
        @Min(value = 1, message = "You must order at least 1 item.")
        @Max(value = 5, message = "You cannot order more than 5 items.")
        Integer quantity
){}
