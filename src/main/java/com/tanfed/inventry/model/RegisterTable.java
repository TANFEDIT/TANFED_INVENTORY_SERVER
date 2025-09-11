package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTable {

	private String godownName;
	private String toGodownName;
	
	private String grnNo;
	private LocalDate grnDate;
	
	private String gtnNo;
	private LocalDate gtnDate;
	
	private String dcWdnRoNo;
	private LocalDate supplierDocDate;
	
	private String dcNo;
	private LocalDate dcDate;
	
	private String invoiceNo;
	private LocalDate invoiceDate;

	private LocalDate ackDate;
	private String ifmsId;
	private String buyerName;
	private String district;
	
	private String productName;
	private List<String> productNameList;
	private String packing;
	
	private String suppliedBags;
	private Double suppliedQty;
	
	private String receivedBags;
	private Double receivedQty;
	private Double basicPrice;
	private Double cgst;
	private Double sgst;
	private Double total;
	private String billNo;
}
