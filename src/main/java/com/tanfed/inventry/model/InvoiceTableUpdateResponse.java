package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceTableUpdateResponse {

	private String invoiceNo;
	private LocalDate date;
	private Double totalQty;
	private String secondPointIfmsid;
	private LocalDate updateDate;
	private LocalDate ackDate;
	private String ifmsStatus;

}
