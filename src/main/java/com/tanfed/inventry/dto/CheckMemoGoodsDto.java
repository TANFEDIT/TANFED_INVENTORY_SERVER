package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GstRateData;
import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.PaymentVoucher;
import com.tanfed.inventry.model.TermsDataForPurchaseBooking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckMemoGoodsDto {

	private String activity;
	private String checkMemoNo;
	private LocalDate cmDate;
	private List<String> designation;
	private String voucherStatus;
	private String productCategory;
	private String productGroup;
	private String productName;
	private String standardUnits;
	private String packing;
	private String hsnCode;
	private String supplierName;
	private String supplierAccountNo;
	private String supplierGst;
	private Long id;
	private String poType;
	private String poMonth;
	private String poNo;
	private LocalDate poDate;
	private Double totalPoQty;
	private String termsNo;
	
	
	private Double advOutstanding;
	private Double gstRate;
	
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
	
	private List<TermsDataForPurchaseBooking> termsData;
	
	private List<TermsDataForPurchaseBooking> termsDataGeneral;
	
	private List<TermsDataForPurchaseBooking> termsDataDirect;
	
	private List<TermsDataForPurchaseBooking> termsDataBuffer;
	
	private Double totalPaymentValue;
	private Double netPaymentValue;
	private Double rate;
	private Double percentageValue;
	private Double netPaymentAfterAdjustment;
	
	private Double difference;
	private String remarks;
	private List<JournalVoucher> jvData;
	private PaymentVoucher pvData;
}
