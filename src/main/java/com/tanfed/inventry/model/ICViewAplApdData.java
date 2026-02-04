package com.tanfed.inventry.model;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICViewAplApdData {

	private List<ICP1Data> invoice;
	private Set<String> icmNoList;
	private List<AdjustmentReceiptVoucher> adjv;
	private String bankName;
	private String door;
	private String street;
	private String district;
	private Long pincode;	
}
