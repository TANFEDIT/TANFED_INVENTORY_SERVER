package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnDataForSupplierInvoice {

	private String godownName;
	private String dcWdnRoNo;
	private String grnNo;
	private Double materialReceivedQuantity;
	private Double remainingQty;
}
