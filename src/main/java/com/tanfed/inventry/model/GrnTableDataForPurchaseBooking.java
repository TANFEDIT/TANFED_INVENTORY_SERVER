package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GrnTableDataForPurchaseBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String godownName;
	private String godownType;
	private String grnNo;
	private LocalDate date;
	private Double materialReceivedQuantity;
	private String supplierInvoiceNo;
	private Double invoiceQty;
	private LocalDate invoiceDate;
	private String suppliedTo;
	private String region;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<NonCCInvoiceTableData> nonCcInvoiceData;
}
