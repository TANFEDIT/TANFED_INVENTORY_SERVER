package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoRegisterTable {

	private String activity;
	private String poType;
	private String poNo;
	private LocalDate date;

	private Double poDirectQty;
	private Double poBufferQty;
	private Double poTotalQty;

	private Double receiptDirectQty;
	private Double receiptBufferQty;
	private Double receiptTotalQty;

	private Double balanceDirectQty;
	private Double balanceBufferQty;
	private Double balanceTotalQty;

	private String supplierName;
	private String productName;
	private String godownName;

	private String grnNoDirect;
	private LocalDate dateDirect;
	private Double qtyDirect;

	private String grnNoBuffer;
	private LocalDate dateBuffer;
	private Double qtyBuffer;

}
