package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierAdvance {

	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private LocalDate approvedDate;

	private List<String> empId;

	private List<String> designation;

	private String voucherStatus;

	private String supplierAdvanceNo;

	private LocalDate date;

	private String activity;

	private String productCategory;

	private String supplierName;

	private String productName;

	private String productGroup;

	private String hsnCode;

	private String supplierGst;

	private String standardUnits;

	private String packing;

	private String termsNo;

	private String termsMonth;

	private Double qty;

	private String selectedOption;

	private Double rate;

	private Double basicPrice;

	private Double gstRate;

	private Double gstValue;

	private Double totalValue;

	private Double tdsOrTcsValue;

	private Double netAdvanceValue;

	private Double multipliedBasicPrice;

	private Double multipliedGstValue;

	private Double totalMultipliedValue;

	private Double totalMultipliedTdsAndTcsValue;

	private Double MultipliedAdvanceValue;

	private Double others;

	private Double netAdvanceValueAfterOthers;

	private Double avlAmountForCheckMemo;

	private Double avlQtyForCheckMemo;

	private PaymentVoucher pv;

	private JournalVoucher jv;
}
