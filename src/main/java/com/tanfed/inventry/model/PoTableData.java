package com.tanfed.inventry.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PoTableData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String region;
	
	private String poReqNo;
	
	private Double requestQuantity;
	
	private Double poIssueQty;
	
	private String issuedFor;

    private Double alreadyIssuedQty = 0.0;
	
	private String oldPoNo;
	
	private String poRequestFor;
}
