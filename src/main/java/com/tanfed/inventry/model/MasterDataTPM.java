package com.tanfed.inventry.model;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MasterDataTPM {

	private LocalDate date;
	
	private String activity;
	
	private String productCategory;
	
	private String standardUnits;
	
	private String productGroup;
	
	private String productName;
	
	private String packing;

	private String hsnCode;

	private String supplierGst;
	
	private String supplierName;
	
	private LocalDate validFrom;
	
	private LocalDate validTo;
	
	private LocalDate withEffectiveDate;
	
	private String termFor;
	
	private String termsForMonth;
	
	private String costingNumber;
}
