package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PurchaseDayBookQtyGrnData {

	private String grnNo;
	private LocalDate grnDate;
	
	private Double direct;
	private Double buffer;

}
