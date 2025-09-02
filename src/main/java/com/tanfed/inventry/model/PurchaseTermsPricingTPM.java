package com.tanfed.inventry.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PurchaseTermsPricingTPM {

	private Double mrp;
	
	private Double margin;
	
	private Double basicPrice;
	
	private Double gstRate;
	
	private Double gstValue;
	
	private Double gstOnMargin;
	
	private Double netPrice;
}
