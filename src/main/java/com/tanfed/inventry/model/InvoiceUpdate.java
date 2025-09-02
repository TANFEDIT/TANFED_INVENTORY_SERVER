package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceUpdate {
	
	private String invoiceNo;
	private String secondPointIfmsid;
	private LocalDate updateDate;
	private LocalDate ackDate;
	private String ifmsStatus;
}
