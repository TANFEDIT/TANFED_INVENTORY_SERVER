package com.tanfed.inventry.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class NonCCInvoiceTableData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private String invoiceNo;
	private LocalDate invoiceDate;
	private Double invoiceQty;
	private Double invoiceAmount;
	private LocalDate dateOfCollection;
}
