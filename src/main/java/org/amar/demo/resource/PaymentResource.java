package org.amar.demo.resource;

import org.amar.demo.exception.PaymentNotFoundException;
import org.amar.demo.model.ErrorResponse;
import org.amar.demo.model.PaymentRequest;
import org.amar.demo.model.PaymentResponse;
import org.amar.demo.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {
	private static final Logger log = LoggerFactory.getLogger(PaymentResource.class);
	private final PaymentService paymentService;

	public PaymentResource(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/**
	 * Initiates a new card payment
	 * 
	 * @param request Payment request containing amount and card details
	 * @return Response with payment ID and initial status
	 */
	@POST
	@Path("/initiate")
	public Uni<Response> initiatePayment(@Valid PaymentRequest request) {
		log.info("Received payment initiation request for amount: {}", request.getAmount());
		return paymentService.initiatePayment(request).onItem()
				.transform(payment -> Response.status(Response.Status.ACCEPTED).entity(payment).build())
				.onFailure(ConstraintViolationException.class)
				.recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorResponse("Validation failed", "VALIDATION_ERROR")).build())
				.onFailure().recoverWithItem(this::handleError);
	}

	/**
	 * Retrieves current status of a payment
	 * 
	 * @param paymentId Unique payment identifier
	 * @return Response with current payment status
	 */
	@GET
	@Path("/status/{paymentId}")
	public Uni<Response> getPaymentStatus(@PathParam("paymentId") String paymentId) {
		log.debug("Fetching payment status for ID: {}", paymentId);
		return paymentService.getPaymentStatus(paymentId).onItem()
				.transform(status -> Response.ok(new PaymentResponse(paymentId, status)).build())
				.onFailure(PaymentNotFoundException.class)
				.recoverWithItem(e -> Response.status(Response.Status.NOT_FOUND)
						.entity(new ErrorResponse(e.getMessage(), "PAYMENT_NOT_FOUND")).build())
				.onFailure().recoverWithItem(this::handleError);
	}

	private Response handleError(Throwable t) {
		log.error("Payment processing error", t);
		return Response.serverError().entity(new ErrorResponse("Internal server error", "INTERNAL_ERROR")).build();
	}
}