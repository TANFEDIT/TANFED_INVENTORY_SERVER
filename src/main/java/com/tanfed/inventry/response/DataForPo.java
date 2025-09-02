package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.PoReqDataForPo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForPo {

	private Set<String> productNameList;
	private Set<String> termsMonthList;
	private List<String> termsNoList;
	private String supplierName;
	private String supplierGst;
	private String standardUnits;
	private String productCategory;
	private String productGroup;
	private String packing;
	private TermsPrice termsPrice;
	
	private Set<String> officeList;
	private List<PoReqDataForPo> poReqTableData;
	
}
