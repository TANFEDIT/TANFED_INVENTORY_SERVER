package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.JournalVoucher;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInvoiceDto {
	private Long id;

	private String voucherStatus;

	private List<String> designation;

	private LocalDate approvedDate;

	private Double invoiceQtyAvlForGrnAttach;

	private List<String> empId;
	
	private LocalDate date;

	private LocalDate createdAt = LocalDate.now();

	private String invoiceNumber;

	private Double invoiceQty;


	private LocalDate invoiceDate;

	private Double totalInvoiceValue;

	private Double totalBasicPrice;

	private Double totalCgstValue;

	private Double totalSgstValue;

	private String monthOfSupply;

	private String termsMonth;
	
	private String termsNo;
	
	private String activity;

	private String productName;

	private String supplierName;

	private String supplierGst;

	private String filename;

	private String filetype;

	@Lob
	@Column(length = 1000000)
	private byte[] filedata;

	private JournalVoucher netJv;

	private JournalVoucher taxJv;
	
}
