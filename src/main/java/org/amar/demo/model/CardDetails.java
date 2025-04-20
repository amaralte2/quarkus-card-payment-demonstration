package org.amar.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardDetails {

	@NotBlank
	@Pattern(regexp = "\\d{16}")
	private String cardNumber;

	@NotBlank
	private String cardHolderName;

	@NotBlank
	@Pattern(regexp = "\\d{2}/\\d{2}")
	private String expiry;

	@NotBlank
	@Pattern(regexp = "\\d{3}")
	private String cvv;

}
