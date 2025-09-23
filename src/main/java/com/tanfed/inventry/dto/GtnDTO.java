package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.model.GtnInvoiceData;
import com.tanfed.inventry.model.GtnTableData;
import com.tanfed.inventry.model.JournalVoucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtnDTO {
	
	private Long id;
	
	private String gtnNo;
	
	private String officeName;
	
	private String voucherStatus;
	
	private List<String> designation;
	
	private List<String> empId;
	
	private LocalDate approvedDate;
	
	
	private String fromIfmsId;
	
	private String toIfmsId;
	
	private LocalDate date;
	
	private String activity;
	
	private String gtnFor;
	
	private String transactionFor;
	
	
	
	
	private String productCategory;
	
	private String productGroup;
	
	private String productName;
	
	private String supplierGst;
	
	private String supplierName;
	
	
	
	private String GodownName;
	
	private String toRegion;
	
	private String destination;
	
	
	
	
	
	private String transporterName;
	
	private Double totalUnloadingCharges;
	
	private Double transportChargesValue;

	private Double transportChargesPerQty;
	
	private Double km;
	
	private Double loadingChargesValue;
	
	private Double loadingChargesPerQty;
	
	
	
	private String rrNo;
		
	private String movementDocDate;
	
	private String vehicleNo;
	
	private String issuedGtnNo;
	
	private String buyerName;
	
	private String buyerDistrict;
	
	private String buyerGstNo;
	
	private String transportCharges;
	
	private String loadingCharges;
	
	private JournalVoucher jv;
	
	private String invoiceNo;
	
	private List<GtnInvoiceData> invoiceTableData;
	
	private List<GtnTableData> gtnTableData;
	
	private Invoice invoice;
	
	private Boolean billEntry;
}
