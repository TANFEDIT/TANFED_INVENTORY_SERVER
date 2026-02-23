package com.tanfed.inventry.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRegisterTable {

	private LocalDate date;
	private Double openingStock;
	private Double receipt;
	private Double otherReceipt;
	private Double total;
	private Double issue;
	private Double otherIssue;
	private Double closingStock;
}
