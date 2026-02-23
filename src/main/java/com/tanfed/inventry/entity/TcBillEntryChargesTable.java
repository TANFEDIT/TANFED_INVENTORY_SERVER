package com.tanfed.inventry.entity;

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
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class TcBillEntryChargesTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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
