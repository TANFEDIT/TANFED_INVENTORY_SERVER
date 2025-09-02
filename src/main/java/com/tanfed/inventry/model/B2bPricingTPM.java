package com.tanfed.inventry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class B2bPricingTPM {
	
	@Column
	private Double b2bBasicPrice;
	
	@Column
	private Double b2bCgst;
	
	@Column
	private Double b2bSgst;
	
	@Column
	private Double b2bMrp;
	
	@Column
	private Double b2bNetTotal;
	
	@Column
	private Double marginToPaccs;
	
	@Column
	private Double paccsMarginGst;
}
