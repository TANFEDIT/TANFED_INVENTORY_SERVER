package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxInfo {

	private Long id;

	private String gstNo;

	private String panNo;

	private String tinNo;

	private String tanNo;

	private String gstCategory;

	private Double gstRate;

	private Double cgstRate;

	private Double sgstRate;

	private Double igstRate;

	private Double rcmRate;

	private LocalDate date;
}
