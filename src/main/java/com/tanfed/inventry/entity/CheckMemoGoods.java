package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GstRateData;
import com.tanfed.inventry.model.TermsDataForPurchaseBooking;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
public class CheckMemoGoods {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private List<String> designation;
	private List<String> empId;
	private String voucherStatus;
	private LocalDate createdAt = LocalDate.now();
	private LocalDate approvedDate;

	private String activity;
	private String checkMemoNo;
	private LocalDate cmDate;

	private String productCategory;
	private String productGroup;
	private String productName;
	private String standardUnits;
	private String packing;
	private String hsnCode;
	private String supplierName;
	private String supplierAccountNo;
	private String supplierGst;

	private String poType;
	private String poMonth;
	private String poNo;
	private LocalDate poDate;
	private Double totalPoQty;
	private String termsNo;

	private Double advOutstanding;
	private Double gstRate;

	@Embedded
	private GstRateData gstData;

	private Double totalGrnQty;
	private Double totalSupplierInvQty;
	private Double jvQty;
	private String purchaseJvNo;

	private String advanceAdjOptions;
	private String supplierAdvanceNo;
	private Double currentAdvanceQty;
	private Double calulatedBasicPrice;
	private Double calculatedTcsTdsValue;
	private Double calculatedTotal;

	private String creditNoteAdjOptions;
	private Double creditNoteAdjAmount;
	private Double creditNoteAdjCnNo;
	private LocalDate creditNoteCnDate;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsData;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataGeneral;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataDirect;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TermsDataForPurchaseBooking> termsDataBuffer;

	private Double totalPaymentValue;
	private Double netPaymentValue;
	private Double rate;
	private Double percentageValue;
	private Double netPaymentAfterAdjustment;

	private Double difference;
	private String remarks;

	private String pvNo;
	private List<String> jvNoList;

}
