package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GrnTableDataForPurchaseBooking;
import com.tanfed.inventry.model.TermsDataForPurchaseBooking;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate approvedDate;
	
	private List<String> empId;
	
	private List<String> designation;
	
	private String voucherStatus;
	
	private String checkMemoNo;

	private Boolean isCheckMemoCreated;
	
	
	
	private LocalDate date;
	
	private String activity;
	
	private String productCategory;
	
	private String supplierName;

	private String productName;
	
	private String poType;
	
	private String poMonth;
	
	private String poNo;
	
	private String productGroup;
	
	private String standardUnits;
	
	private String packing;
	
	private String termsNo;
	
	private Double totalPoQty;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<GrnTableDataForPurchaseBooking> grnTableData;
	
	private Double totalQty;
	
	private Double directQty;
	
	private Double bufferQty;
	
	private Double inputTax;
	
	private Double margin;
	
	private Double deduction;
	
	private Double net;
	
	private Double tradeIncome;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsData;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataGeneral;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataDirect;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataBuffer;
	
	private List<String> jvList;
	
	
}
