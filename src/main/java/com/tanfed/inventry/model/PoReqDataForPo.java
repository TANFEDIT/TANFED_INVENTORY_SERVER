package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoReqDataForPo {

	private String poReqNo;
	private String oldPoNo;
	private String poRequestFor;
	private Double reqQty;
	private Double alreadyIssuedQty;
}
