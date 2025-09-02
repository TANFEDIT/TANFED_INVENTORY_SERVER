package com.tanfed.inventry.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
public class PoReqTempTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String poReqNo;
    
    private String productName;
    
    private String poRequestFor;
    
    private Double requestQuantity;
    
    private Double alreadyIssuedQty;

    private Double issueQty;
    
    private LocalDateTime createdAt;
    
    private String officeName;
    
    private String poReqType;
}
