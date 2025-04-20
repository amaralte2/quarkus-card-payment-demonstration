package org.amar.demo.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.amar.demo.exception.PaymentNotFoundException;
import org.amar.demo.model.CardDetails;
import org.amar.demo.model.PaymentRequest;
import org.amar.demo.model.PaymentResponse;
import org.amar.demo.model.PaymentStatus;
import org.amar.demo.service.PaymentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.smallrye.mutiny.Uni;

@QuarkusTest
class PaymentResourceTest {

	@InjectMock
	PaymentService paymentService;

	@BeforeAll
	public static void setup() {
		RestAssured.defaultParser = Parser.JSON;
	}

	@Test
	void testInitiatePaymentSuccess() {
		PaymentRequest request = new PaymentRequest(new CardDetails("1234567890123456", "Amar Alte", "12/25", "123"),
				100.0);

		PaymentResponse mockResponse = new PaymentResponse("payment-id-123", PaymentStatus.IN_PROGRESS);
		Mockito.when(paymentService.initiatePayment(Mockito.any(PaymentRequest.class)))
				.thenReturn(Uni.createFrom().item(mockResponse));

		given().contentType(ContentType.JSON).body(request).when().post("/api/payment/initiate").then().statusCode(202)
				.body("paymentId", equalTo(null)).body("status", equalTo("IN_PROGRESS"));
	}

	@Test
	void testInitiatePaymentValidationFailure() {
		// Create an invalid request with negative amount and invalid card details
		PaymentRequest invalidRequest = new PaymentRequest(new CardDetails("invalid", "", "", ""), -10.0);

		given().contentType(ContentType.JSON).body(invalidRequest).when().post("/api/payment/initiate").then()
				.statusCode(400) // Expecting HTTP 400 Bad Request
				.body("errorCode", equalTo(null)).body("message", equalTo(null));
	}

	@Test
	void testGetPaymentStatusSuccess() {
		String paymentId = "test-id-123";
		Mockito.when(paymentService.getPaymentStatus(paymentId))
				.thenReturn(Uni.createFrom().item(PaymentStatus.COMPLETED));

		given().when().get("/api/payment/status/" + paymentId).then().statusCode(200)
				.body("paymentId", equalTo(null)).body("status", equalTo("COMPLETED"));
	}

	@Test
	void testGetPaymentStatusNotFound() {
		String invalidId = "non-existent-id";
		Mockito.when(paymentService.getPaymentStatus(invalidId))
				.thenReturn(Uni.createFrom().failure(new PaymentNotFoundException("Payment not found")));

		given().when().get("/api/payment/status/" + invalidId).then().statusCode(404)
				.body("errorCode", equalTo("PAYMENT_NOT_FOUND")).body("message", equalTo("Payment not found"));
	}

	@Test
	void testInitiatePaymentInternalServerError() {
		PaymentRequest request = new PaymentRequest(new CardDetails("1234567890123456", "Amar Alte", "12/25", "123"),
				100.0);

		Mockito.when(paymentService.initiatePayment(Mockito.any(PaymentRequest.class)))
				.thenReturn(Uni.createFrom().failure(new RuntimeException("Unexpected error")));

		given().contentType(ContentType.JSON).body(request).when().post("/api/payment/initiate").then().statusCode(500)
				.body("errorCode", equalTo("INTERNAL_ERROR")).body("message", equalTo("Internal server error"));
	}
}