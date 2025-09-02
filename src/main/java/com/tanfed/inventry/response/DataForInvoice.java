package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.InvoiceTermsAndConditions;
import com.tanfed.inventry.model.InvoiceDataTable;
import com.tanfed.inventry.model.InvoiceTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForInvoice {

	private List<String> dcNoList;
	private List<InvoiceTable> tableData;
	private Set<String> godownNameList;
	private Double totalNoOfBags;
	private Double totalQty;
	private Double totalBasicValue = 0.0;
	private Double totalCgstValue = 0.0;
	private Double totalSgstValue = 0.0;
	private Double totalInvoiceValue = 0.0;
	private Double totalMarginValue = 0.0;
	private Double totalGstOnMargin = 0.0;
	
	private String ifmsId;
	private String nameOfInstitution;
	private String licenseNo;
	private String buyerGstNo;
	private String village;
	private String block;
	private String taluk;
	private String district;
	private String supplyTo;
	private String supplyMode;
	private String despatchAdviceNo;
	private String salesType;
	private Set<String> creditPeriodList;
	private List<InvoiceTermsAndConditions> tcData;
	private List<InvoiceDataTable> invoiceData;
	private Double b2cDiscount;
}
