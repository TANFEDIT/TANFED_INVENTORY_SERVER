package com.tanfed.inventry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanfed.inventry.entity.SalesReturn;

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
public class GtnInvoiceData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String grnNo;
	private String productName;
	private Double basicPrice;
	private Double cgstAmount;
	private Double sgstAmount;
	private Double qty;
	private Double QtyAvlForDc;
	private Double returnQty;

	@ManyToOne
	@JoinColumn(name = "salesReturn")
	@JsonIgnore
	private SalesReturn salesReturn;
}
