package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.B2bPricingTPM;
import com.tanfed.inventry.model.B2bTermsConditionsTPM;
import com.tanfed.inventry.model.B2cPricingTPM;
import com.tanfed.inventry.model.MasterDataTPM;
import com.tanfed.inventry.model.PurchaseTermsConditionsTPM;
import com.tanfed.inventry.model.PurchaseTermsPricingTPM;
import com.tanfed.inventry.model.QtyRebate;
import com.tanfed.inventry.model.TermsData;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class TermsPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate approvedDate;
	private LocalDate extentionDate;
	@Column
	private List<String> empId;
	
	@Column
	private String termsNo;
	
	@Column
	private String circularNo;
	
	@Column
	private String voucherStatus = "Pending";
	
	@Column
	private List<String> designation;
	
	@Embedded
	private MasterDataTPM masterData;
	
	@Embedded
	private PurchaseTermsConditionsTPM purchaseTermsAndCondition;
	
	@Embedded
	private PurchaseTermsPricingTPM purchaseTermsPricing;
	
	@Embedded
	private B2bTermsConditionsTPM b2bTermsAndConditions;
	
	@Embedded
	private B2bPricingTPM b2bPrice;
	
	@Embedded
	private B2cPricingTPM b2cPrice;
	
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL) 
	private List<QtyRebate> inputs;
	
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private List<TermsData> purchaseDataDirect;
	
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private List<TermsData> purchaseDataBuffer;
	
	@OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private List<TermsData> purchaseDataGeneral;
}
