package com.tanfed.inventry.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable

public class GstRateData {

	private Double cgstRate;
	private Double sgstRate;
	private Double igstRate;
	private Double rcmRate;
}
