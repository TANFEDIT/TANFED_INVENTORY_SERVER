package com.tanfed.inventry.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDataForDnCn {

	private LocalDate invoiceDate;
	private String gst;
	private Double invoiceValue;
	private String name;
	private String ifmsId;
}
