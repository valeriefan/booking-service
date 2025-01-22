package rentsphere.bookingservice.booking.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestValidationTests {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		var bookingRequest = new BookingRequest("123456789", 1);
		Set<ConstraintViolation<BookingRequest>> violations = validator.validate(bookingRequest);
		assertThat(violations).isEmpty();
	}

	@Test
	void whenCodeNotDefinedThenValidationFails() {
		var bookingRequest = new BookingRequest("", 1);
		Set<ConstraintViolation<BookingRequest>> violations = validator.validate(bookingRequest);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("The house reference code must be defined.");
	}

	@Test
	void whenQuantityIsNotDefinedThenValidationFails() {
		var bookingRequest = new BookingRequest("123456789", null);
		Set<ConstraintViolation<BookingRequest>> violations = validator.validate(bookingRequest);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("The house quantity must be defined.");
	}

	@Test
	void whenQuantityIsLowerThanMinThenValidationFails() {
		var bookingRequest = new BookingRequest("123456789", 0);
		Set<ConstraintViolation<BookingRequest>> violations = validator.validate(bookingRequest);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("You must order at least 1 item.");
	}

	@Test
	void whenQuantityIsGreaterThanMaxThenValidationFails() {
		var bookingRequest = new BookingRequest("123456789", 7);
		Set<ConstraintViolation<BookingRequest>> violations = validator.validate(bookingRequest);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
				.isEqualTo("You cannot order more than 5 items.");
	}

}
