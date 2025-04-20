package org.amar.demo.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.amar.demo.exception.PaymentNotFoundException;
import org.amar.demo.model.PaymentRequest;
import org.amar.demo.model.PaymentResponse;
import org.amar.demo.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentService {
	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
	private final Map<String, PaymentStatus> paymentStore = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

	/**
	 * Initiates a new payment transaction
	 * 
	 * @param request Payment request containing amount and card details
	 * @return Uni containing PaymentResponse with initial status
	 */
	public Uni<PaymentResponse> initiatePayment(PaymentRequest request) {
		return Uni.createFrom().item(() -> {
			String paymentId = UUID.randomUUID().toString();
			paymentStore.put(paymentId, PaymentStatus.IN_PROGRESS);

			// Payment processing with delay
			executor.schedule(() -> processPayment(paymentId, request), 3, TimeUnit.SECONDS);

			log.info("Payment initiated - ID: {}, Amount: {}", paymentId, request.getAmount());
			return new PaymentResponse(paymentId, PaymentStatus.IN_PROGRESS);
		}).runSubscriptionOn(Infrastructure.getDefaultExecutor());
	}

	/**
	 * Retrieves current payment status by payment ID
	 * 
	 * @param paymentId Unique payment identifier
	 * @return Uni containing current PaymentStatus
	 * @throws PaymentNotFoundException if payment ID not found
	 */
	public Uni<PaymentStatus> getPaymentStatus(String paymentId) {
		return Uni.createFrom().item(() -> {
			PaymentStatus status = paymentStore.get(paymentId);
			if (status == null) {
				throw new PaymentNotFoundException("Payment not found with ID: " + paymentId);
			}
			return status;
		}).runSubscriptionOn(Infrastructure.getDefaultExecutor());
	}

	private void processPayment(String paymentId, PaymentRequest request) {
		try {
			log.debug("Processing payment: {}", paymentId);
			// Simulate validation logic
			if (isValidPayment(request)) {
				paymentStore.put(paymentId, PaymentStatus.COMPLETED);
				log.info("Payment completed: {}", paymentId);
			} else {
				paymentStore.put(paymentId, PaymentStatus.FAILED);
				log.warn("Payment failed: {}", paymentId);
			}
		} catch (Exception e) {
			paymentStore.put(paymentId, PaymentStatus.FAILED);
			log.error("Payment processing error: {}", paymentId, e);
		}
	}

	private boolean isValidPayment(PaymentRequest request) {
		return request.getAmount() > 0 && request.getCardDetails().getCardNumber().length() == 16
				&& !request.getCardDetails().getCardNumber().startsWith("9");
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
