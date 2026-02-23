package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.WagonDataGrn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnDto {
	private Long id;

	private String grnNo;

	private String voucherStatus;

	private List<String> empId;

	private List<String> designation;

	private String officeName;

	private LocalDate createdAt;

	private LocalDate approvedDate;

	private LocalDate date;

	private String activity;

	private String productName;

	private String godownType;

	private String supplierName;

	private String godownName;

	private String ifmsId;

	private String supplyFrom;

	private String productCategory;

	private String productGroup;

	private String supplierGst;

	private String standardUnits;

	private String door;

	private String street;

	private Integer pincode;

	private String district;

	private String poNo;

	private String modeOfSupply;

	private Double totalPoQty;

	private Double grnCreated;

	private String dcWdnRoNo;

	private LocalDate supplierDocDate;

	private String supplierTransport;

	private String vehicleNo;

	private String transportCharges;

	private String unloadingCharges;

	private Double unloadingChargesValue;

	private Double wagonClearanceValue;

	private String materialReceivedBags;

	private Double materialReceivedQuantity;

	private String packing;

	private String materialSuppliedBags;

	private Double materialSuppliedQuantity;

	private String batchOrCertificateNo;

	private LocalDate expiryDate;

	private String batchNo;

	private String certification;

	private String grnIfmsId;
	private String billNo;

	private String firstPointIfmsId;

	private LocalDate idCreateDate;

	private LocalDate ackDate;

	private String ifmsStatus;

	private Double grnQtyAvlForGrnAttach;

	private Double grnAttachQty;

	private String grnAttachQtyString;

	private String supplierInvoiceNo;

	private Double grnQtyAvlForDc;

	private WagonDataGrn wagonData;

	private JournalVoucher jv;

	private Boolean isPurchaseBooked;

	private Boolean unloadingBillEntry;

	private Boolean wagonBillEntry;
}
