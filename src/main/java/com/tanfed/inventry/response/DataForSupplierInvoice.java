package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tanfed.inventry.dto.SupplierInvoiceInfoGrnAttach;
import com.tanfed.inventry.model.GrnDataForSupplierInvoice;
import com.tanfed.inventry.model.PurchaseTermsPricingTPM;
import com.tanfed.inventry.model.TermsData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForSupplierInvoice {

	private Set<String> supplierNameList;
	private List<String> productNameList;
	private String supplierGst;
	private String productCategory;
	private String productGroup;
	private String packing;
	private String standardUnits;
	private Set<String> poMonthList;
	private Set<String> monthOfSupplyList;
	private List<String> poNoList;
	private List<String> invoiceNoList;
	private Set<String> officeNameList;
	private Set<String> termsMonthList;
	private List<String> termsNoList;
	private PurchaseTermsPricingTPM purchaseTermsPricing;
	private List<TermsData> purchaseDataDirect;
	private List<TermsData> purchaseDataBuffer;
	private List<TermsData> purchaseDataGeneral;
	private LocalDate termsDate;
	private Double poQty;
	private Double totalGrnQty;
	private Double totalBookedQty;
	private Double gstRate;
	private Double invoiceQty;
	private Double invoiceAvlQty;
	private LocalDate invoiceDate;
	private LocalDate poDate;
	private List<GrnDataForSupplierInvoice> grnTableData;
	private List<SupplierInvoiceInfoGrnAttach> invoiceTableData;
}
