package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class MpaEmployeeData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private List<String> empId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mpa_id", nullable = false)
	private ManPowerAgency mpaData;

	private String empName;

	private String aadharNo;

	private String eduQualification;

	private String engagedAs;

	private String serviceChargePercentage;

	private String serviceChargeFlat;

	private String rateDefinedAs;

	private String approvedRate;

	private String status;

}
