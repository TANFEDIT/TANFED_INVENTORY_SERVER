package com.tanfed.inventry.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class TableDataInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	 
	private Double bags;
	
	 
	private Double basicPrice;
	
	 
	private String batchcertNo;
	
	 
	private String productCategory;
	
	 
	private Double cgstAmount;
	
	 
	private String grnNo;
	
	 
	private Double gstRate;
	
	 
	private String hsnCode;
	
	 
	private Double margin;
	
	 
	private Double mrp;
	
	 
	private String packing;
	
	 
	private String productName;
	
	 
	private Double qty;
	
	 
	private Double sgstAmount;
	
	 
	private String supplierName;
	
	 
	private Double total;
}
