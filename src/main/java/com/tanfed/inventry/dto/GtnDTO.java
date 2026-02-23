package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.model.GtnInvoiceData;
import com.tanfed.inventry.model.JournalVoucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtnDTO {

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

	private JournalVoucher jv;

	private String invoiceNo;

	private Invoice invoice;

	private List<GtnInvoiceData> invoiceTableData;

	private Boolean billEntry;
}
