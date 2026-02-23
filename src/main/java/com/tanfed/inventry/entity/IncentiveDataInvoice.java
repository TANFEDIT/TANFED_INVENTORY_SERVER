package com.tanfed.inventry.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IncentiveDataInvoice {

	private String incentivePaccsPm;

	private String secretoryAndSalesmanPm;

	private Double incentivePaccsTotal;

	private Double secretoryIncentiveTotal;

	private Double salesmanIncentiveTotal;
}
