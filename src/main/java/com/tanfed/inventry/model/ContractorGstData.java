package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ContractorGstData {

	private Long id;

	private String gstRateFor;

	private String gstCategory;

	private Double gstRate;

	private Double sgstRate;

	private Double cgstRate;

	private Double igstRate;
}
