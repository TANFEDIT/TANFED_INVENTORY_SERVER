package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.ProductData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForTPM {

	private Set<String> supplierList;
	private List<String> headNameDirectList;
	private List<String> headNameBufferList;
	private List<String> headNameGeneralList;
	private List<String> supplyModeList;
	private List<String> paymentModeList;
	private List<String> productNameList;
	private ProductData productData;
	private Double gstRate;
}
