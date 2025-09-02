package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Office {
	private String officeCode;
	private String officeType;
	private String officeName;
	private String code;
	private String activity;
}
