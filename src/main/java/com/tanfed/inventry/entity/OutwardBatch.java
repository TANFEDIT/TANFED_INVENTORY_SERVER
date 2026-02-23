package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class OutwardBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime time;

	private String dcNo;

	private String voucherId;

	private String batchNo;

	private Double qty;

	private String productCategory;

	private String productGroup;

	private String supplierName;

	private String productName;

	private String packing;

	private String standardUnits;

	private Double mrp;

	private LocalDate grnDate;

	private String officeName;

	private String godownName;

	private String daNo;

}
