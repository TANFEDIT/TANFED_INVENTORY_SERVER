package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GtnInvoiceData;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class SalesReturn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private String officeName;

	private String voucherStatus;

	private List<String> designation;

	private List<String> empId;

	private LocalDate approvedDate;

	private LocalDate date;

	private String gtnNo;

	private String activity;

	private String month;

	private String suppliedGodown;

	private String godownName;

	private String jvNo;

	private String invoiceNo;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "invoice_id")
	private Invoice invoice;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "salesReturn")
	private List<GtnInvoiceData> invoiceTableData;
	private Boolean billEntry;
}
