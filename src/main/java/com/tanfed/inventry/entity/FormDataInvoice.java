package com.tanfed.inventry.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FormDataInvoice {

	  
	private String block;
	
	  
	private Double cgstValue;
	
	  
	private String collection;
	
	  
	private Integer creditPeriod;
	
	  
	private String dcNo;
	
	
	private String collectionMode;
	

	private String despatchAdviceNo;
	
	  
	private String district;
	
	  
	private String gst;
	
	  
	private String ifOthers;
	
	  
	private String ifmsId;
	
	  
	private String firmType;
	
	  
	private String invoiceFor;
	
	  
	private String invoiceTo;
	
	  
	private String name;
	
	  
	private String selectActivity;
	
	  
	private Double sgstValue;
	
	  
	private String supplyMode;
	
	  
	private String taluk;
	
	  
	private String tcHt;
	
	  
	private Integer totalBags;
	
	  
	private Double totalBasicValue;
	
	  
	private Double totalGstOnMargin;
	
	  
	private Double totalInvoiceValue;
	
	  
	private Double totalMarginValue;
	
	  
	private String materialCenter;
	
	  
	private Double totalQty;
	
	  
	private String village;
}
