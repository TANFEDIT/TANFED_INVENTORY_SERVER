package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.DespatchAdviceData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForDespatchAdvice {

	private List<String> nameOfInstitutionList;
	private List<String> productNameList;
	private Set<String> godownNameList;
	private List<String> supplyModeList;
	private String district;
	private String taluk;
	private String block;
	private String village;
	private String buyerGstNo;
	private String ifmsId;
	private String supplyTo;
	private String packing;
	private String productCategory;
	private String productGroup;
	private String standardUnits;
	private String licenseNo;
	private List<DespatchAdviceData> despatchAdviceData;
}
