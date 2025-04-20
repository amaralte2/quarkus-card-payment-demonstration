package org.amar.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents the status of a card payment.
 */
public class PaymentResponse {
	private String transactionId;
	private PaymentStatus status;

}
