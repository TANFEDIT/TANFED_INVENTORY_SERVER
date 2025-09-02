package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.GrnTableDataForPurchaseBooking;
import com.tanfed.inventry.model.TermsDataForPurchaseBooking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForPurchaseBooking {

	private Set<String> productCategoryList;
	
	private List<String> productNameList;
	
	private Set<String> supplierNameList;
	
	private Set<String> poMonthList;
	
	private List<String> poNoList;
	
	private String supplierGst;
	private String productGroup;
	private String packing;
	private String standardUnits;
	
	private String termsNo;
	private Double totalPoQty;
	private Double bookedQty;
	private Double avlQty;
	
	private List<GrnTableDataForPurchaseBooking> grnTableData;
	
	private Double totalQty;
	private Double directQty;
	private Double bufferQty;
	
	private List<TermsDataForPurchaseBooking> termsData;
	private List<TermsDataForPurchaseBooking> termsDataGeneral;
	private List<TermsDataForPurchaseBooking> termsDataDirect;
	private List<TermsDataForPurchaseBooking> termsDataBuffer;
	
	private Double inputTax;
	private Double margin;
	private Double deduction;
	private Double net;
	private Double tradeIncome;
}
