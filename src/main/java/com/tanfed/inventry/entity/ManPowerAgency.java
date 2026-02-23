package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GstRateData;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ManPowerAgency {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private List<String> empId;

	private String officeName;

	private String appointedThrough;

	private String manPowerFor;

	private String manPowerUsedTo;

	private String contractFirm;

	private String appointedLr;

	private String lrDate;

	private String gstNo;

	private String gstReturnType;

	private String gstCategory;

	private String gstRate;

	@Embedded
	private GstRateData gstData;

}
