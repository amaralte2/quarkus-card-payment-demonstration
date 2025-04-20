package org.amar.demo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents a payment request.
 */
public class PaymentRequest {
	@Valid
	private CardDetails cardDetails;

	@NotNull
	@Positive
	public Double amount;
}
