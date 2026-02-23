package com.tanfed.inventry.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class MpaBillEntryEmpTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String engagedAs;

	private String empName;

	private String aadharNo;

	private Double approvedRate;

	private String rateDefinedAs;

	private Double attendance;

	private Double totalAmount;

	private Double deduction;

	private Double afterDeduction;

	private Double serviceCharge;

	private Double grossAmount;
}
