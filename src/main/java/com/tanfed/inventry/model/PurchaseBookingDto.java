package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseBookingDto {

	private Long id;

	private LocalDate date;

	private String activity;

	private List<String> designation;

	private String voucherStatus;

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

	private List<GrnTableDataForPurchaseBooking> grnTableData;

	private Double totalQty;

	private Double directQty;

	private Double bufferQty;

	private Double inputTax;

	private Double margin;

	private Double deduction;

	private Double net;
	private Double others;
	private Double netPriceAfterDeduction;
	private Double tradeIncome;

	private List<TermsDataForPurchaseBooking> termsData;

	private List<TermsDataForPurchaseBooking> termsDataGeneral;

	private List<TermsDataForPurchaseBooking> termsDataDirect;

	private List<TermsDataForPurchaseBooking> termsDataBuffer;

	private List<JournalVoucher> jvData;
}
