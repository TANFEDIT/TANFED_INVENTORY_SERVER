package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespatchAdviseTable {

	private String productName;
	private String dcNo;
	private String invoiceNo;
	private Double qty;
	
}
