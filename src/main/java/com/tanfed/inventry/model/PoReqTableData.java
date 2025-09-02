package com.tanfed.inventry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PoReqTableData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String poRequestFor;

    @Column
    private String productCategory;

    @Column
    private String productGroup;

    @Column
    private String productName;

    @Column
    private Double requestQuantity;

    @Column
    private String standardUnits;

    @Column
    private String supplierName;

    @Column
    private String supplierGst;

    @Column
    private Double alreadyIssuedQty;
}
