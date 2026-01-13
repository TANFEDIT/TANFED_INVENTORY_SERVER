package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDataMovementRegister {

	private String productName;
	private String bags;
	private String qty;
	private String outwardBatchNo;
}
