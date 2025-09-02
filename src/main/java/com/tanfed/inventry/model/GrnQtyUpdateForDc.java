package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnQtyUpdateForDc {

	private String dcNo;
	private String outwardBatchNo;
	private Double qty;
	private String productName;

}
