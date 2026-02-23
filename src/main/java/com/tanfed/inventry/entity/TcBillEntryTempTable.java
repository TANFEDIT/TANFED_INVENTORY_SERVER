package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.ProductClassificationTableBillEntry;

import jakarta.persistence.CascadeType;
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
public class TcBillEntryTempTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();
	private List<String> empId;
	private String claimBillNo;
	private LocalDate claimBillDate;
	private Double totalQty;
	private Double totalBillValue;

	private Double totalTransportCharges;
	private Double totalLoadingCharges;
	private Double totalUnloadingCharges;
	private Double totalWagonClearanceCharges;

	private String claimFor;
	private String idNo;
	private Double ackQty;
	private Double disallowedQty;

	private Double calcTransportCharges;
	private Double calcLoadingCharges;
	private Double calcUnloadingCharges;
	private Double calcWagonClearanceCharges;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<ProductClassificationTableBillEntry> tableData;
}
