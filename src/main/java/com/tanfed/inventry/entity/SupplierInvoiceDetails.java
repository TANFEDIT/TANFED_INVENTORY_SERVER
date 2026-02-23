package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class SupplierInvoiceDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	private String invoiceOfficeName;

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

	private String netJv;

	private String taxJv;

}
