package com.tanfed.inventry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PurchaseTermsConditionsTPM {

	@Column
	private String purchaseCreditDays;

	@Column
	private String purchaseModeofSupply;

	@Column
	private String purchasePaymentMode;

	@Column
	private String rebateReceivableMode;

	@Column
	private String incentiveToB2b;

	@Column
	private String incentiveToB2c;

	@Column
	private String incentiveToFirm;

	@Column
	private String incentiveToTanfed;
}
