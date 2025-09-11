package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespatchAdviceRegisterTable {

	private String activity;
	private String despatchAdviceNo;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private String godownName;
	private Map<String, Double> productAndQty;
	private List<String> dcNoList;
	
	private LocalDate clearedOn;
}
