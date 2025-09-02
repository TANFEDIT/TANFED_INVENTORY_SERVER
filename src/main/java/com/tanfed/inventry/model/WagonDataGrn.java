package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WagonDataGrn {

	private String wagonClearedBy;
	private String shortageAcceptedBy;
	private String rrNo;
	private LocalDate rrDate;
	private Double rrQty;
	private Double tanfedAllottedQty;
	private Double actualReceiptQty;
	private Double actualReceiptBags;
	private Double excessOrShortageQty;
	private Double tanfedAllottedBags;
	private Double noOfBags;
	private Double ratePerMT;
	private Double calcValue;
	private LocalDate dateOfArrival;
	private LocalDate clearanceStartingDate;
	private LocalDate possibleClearanceEndDate;
	private String wagonStatus;
	private List<String> wagonDesignation;
	private LocalDate wagonApprovedDate;
}
