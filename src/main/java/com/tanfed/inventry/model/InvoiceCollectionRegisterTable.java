package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.dto.FT_Charges_Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCollectionRegisterTable {

	private String activity;
	private String godownName;
	private String invoiceNo;
	private LocalDate date;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private Double qty;
	private List<Double> value;
	private LocalDate dueDate;
	private LocalDate ackEntryDate;
	private LocalDate dateOfPresent;
	private List<LocalDate> dateOfCollection;
	private String invoiceType;
	private String branchName;
	private List<String> adjNo;
	private String icmNo;
	private List<LocalDate> adjDate;
	private LocalDate icmDate;
	private FT_Charges_Dto fert;
	private FT_Charges_Dto agriMark;
	private FT_Charges_Dto spai;
	private FT_Charges_Dto total;
}
