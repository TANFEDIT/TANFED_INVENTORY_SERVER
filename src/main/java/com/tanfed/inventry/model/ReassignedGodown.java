package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReassignedGodown {

	private Long id;
	
	 
	private List<String> additionalGodown;
	
	 
	private String rateDefinedAs;
	
	 
	private String contractorName;
}
