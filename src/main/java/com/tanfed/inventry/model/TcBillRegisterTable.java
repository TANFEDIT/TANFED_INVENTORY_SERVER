package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TcBillRegisterTable {

	private String checkMemoNo;
	private String billMonth;
	private String billNo;
	private LocalDate billDate;
	private Double billAmount;
	private Double transportCharges;
	private Double loadingCharges;
	private Double unloadingCharges;
	private Double wagonCharges;
	private Double totalCharges;
	private Double disallowedCharges;
	private Double totalCgst;
	private Double totalSgst;
	private Double grossAmount;
	private Double incomeTax;
	private Double netAmount;
	private LocalDate transferDate;
}
