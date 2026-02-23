package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoObj {

	private String poNo;
	private LocalDate date;
	private String poBased;
	private String circularNo;
	private String poType;
	private String product;
	private String productCategory;
	private String productGroup;
	private Double directQty;
	private Double bufferQty;

}
