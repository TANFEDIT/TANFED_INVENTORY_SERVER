package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForPoRequest {

	private List<String> supplierNameList;
	private Set<String> productNameList;
	private List<String> districtList;
	private String supplierGst;
	private String standardUnits;
	private String productCategory;
	private String productGroup;
	private List<String> poNoList;
	private Double poQty;
	private Double consumedQty;
	private Long PoPendingFromHO;
}
