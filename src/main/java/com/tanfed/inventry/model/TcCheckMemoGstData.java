package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCheckMemoGstData {

	private String gstRateFor;

	private String gstCategory;

	private Double gstRate;

	private Double sgstRate;

	private Double cgstRate;

	private Double igstRate;

	private Double cgstvalue;
	private Double sgstvalue;
}
