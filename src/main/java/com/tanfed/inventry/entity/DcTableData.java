package com.tanfed.inventry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DcTableData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String termsNo;
	
	private String outwardBatchNo;
	
	private String productCategory;

	private String supplierName;

	private String productName;

	private String packing;

	private Double bags;

	private Double qty;

	private String hsnCode;

	private Double gstRate;
	
	private Double mrp;
	
	private Double disallowedQty;
	
	private String productGroup;
	
	private String collectionMode;
}
