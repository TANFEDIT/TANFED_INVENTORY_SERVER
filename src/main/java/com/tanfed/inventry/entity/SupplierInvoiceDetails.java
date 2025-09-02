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
	
	private String invoiceNumber;
	
	private Double invoiceQty;
	
	private Double invoiceQtyAvlForGrnAttach;
	
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate invoiceDate;
	
	private Double invoiceValue;
	private LocalDate date;
	
	private Double basicPrice;
	
	private Double gst;
	
	private Double margin;
	
	private Double gstOnMargin;
	
	private Double mrp;
	
	private String monthOfSupply;
	
	private String activity;
	
	private String productName;
	
	private String supplierName;
	
	private String supplierGst;
	
	private String filename;
	
	private String filetype;
	
	@Lob
	@Column(length = 1000000)
	private byte[] filedata;
	
	private String voucherStatus;
	
	private String designation;
	
	private List<String> empId;
	private LocalDate approvedDate;
}
