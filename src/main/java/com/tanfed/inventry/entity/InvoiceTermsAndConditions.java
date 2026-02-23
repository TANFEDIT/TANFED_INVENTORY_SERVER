package com.tanfed.inventry.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class InvoiceTermsAndConditions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(cascade = CascadeType.ALL)
	private PmodeAndValue incentivePaccs;

	@OneToOne(cascade = CascadeType.ALL)
	private PmodeAndValue salesmanIncentive;

	@OneToOne(cascade = CascadeType.ALL)
	private PmodeAndValue secretoryIncentive;

	private String selectedProductName;
	private Double qty;
}
