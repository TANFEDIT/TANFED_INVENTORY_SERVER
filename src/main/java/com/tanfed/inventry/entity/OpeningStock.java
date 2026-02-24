package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OpeningStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private String voucherStatus = "Pending";

	private List<String> designation;

	private String officeName;

	private List<String> empId;

	private LocalDate approvedDate;

	private String activity;

	private String supplierName;

	private String productName;

	private String productCategory;

	private String productGroup;

	private String supplierGst;

	private Double quantity;

	private Double bookValue;

	private LocalDate asOn;

	private String productStatus;

	private LocalDate expiryDate;
	private String packing;
	private String standardUnits;
	private String obId;

	private String batchOrCertificateNo;
	private String godownName;

	private Double qtyAvlForDc;

//	B2bTermsConditionsTPM

	private String b2bModeofSupply;

	private String b2bCollectionMode;

	private Double incentivePaccs;

	private Double salesmanIncentive;

	private Double secretoryIncentive;

//	B2bPricingTPM	

	private Double b2bBasicPrice;

	private Double b2bCgst;

	private Double b2bSgst;

	private Double b2bMrp;

	private Double b2bNetTotal;

	private Double marginToPaccs;


//	B2cPricingTPM

	private Double b2cBasicPrice;

	private Double b2cCgst;

	private Double b2cSgst;

	private Double b2cMrp;

	private Double b2cNetTotal;

	private Double b2cDiscount;
	private Double mrp;
	private Double supplyPriceToPaccs;

	@PrePersist
	protected void onCreate() {
		this.qtyAvlForDc = quantity;
	}
}
