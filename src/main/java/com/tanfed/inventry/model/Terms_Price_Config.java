package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Terms_Price_Config {

	private Long id;
	
	private LocalDate date = LocalDate.now();
	
	private String supplyType;
	
	private String headName;
	
	private String supplyMode;
	
	private String activity;
	
	private String paymentMode;
	
	private String salesCreditPeriod;
	private String purchaseCreditPeriod;
	
	private List<String> empId;
}
