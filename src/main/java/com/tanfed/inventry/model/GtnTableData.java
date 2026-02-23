package com.tanfed.inventry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanfed.inventry.entity.GTN;

import jakarta.persistence.Entity;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class GtnTableData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String outwardBatchNo;
	private Double mrp;
	private Double qty;
	private Double bags;
	private Double receivedBags;
	private Double receivedQty;
	private String packing;
	private String standardUnits;
	private Double QtyAvlForDc;
	private String collectionMode;
	private String termsNo;
	private String voucherId;

	@ManyToOne
	@JoinColumn(name = "gtn")
	@JsonIgnore
	private GTN gtn;
}
