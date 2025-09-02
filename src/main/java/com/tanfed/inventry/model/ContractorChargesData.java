package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorChargesData {

	private Long id;
	
	 
	private LocalDate rateFrom;
	
	 
	private LocalDate rateTo;
	
	 
	private LocalDate updateDate;
	
	
	
	
	 
	private Double wagonClearance;
	
	 
	private Double loadingCharges;
	
	 
	private Double unloadingCharges;
	
	 
	private Double hillRate;
	
	
	
	
	 
	private Double zero_seven;
	
	 
	private Double eight_twenty;
	
	 
	private Double twentyone_fifty;
	
	 
	private Double fiftyone_seventyfive;
	
	 
	private Double seventysix_hundred;
	
	 
	private Double hundredone_onetwentyfive;
	
	 
	private Double onetwosix_onefifty;
	
	 
	private Double onefiftyone_oneseventyfive;
	
	 
	private Double oneseventysix_twohundred;
	
	 
	private Double abovetwohundredone;
}
