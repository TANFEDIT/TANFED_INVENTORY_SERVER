package com.tanfed.inventry.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class B2bTermsConditionsTPM {

	private String b2bModeofSupply;

	private String b2bCollectionMode;

	private Double incentivePaccs;

	private Double salesmanIncentive;

	private Double secretoryIncentive;

}
