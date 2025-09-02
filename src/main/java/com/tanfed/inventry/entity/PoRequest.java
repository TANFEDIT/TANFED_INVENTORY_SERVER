package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.PoReqTableData;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poReqNo;
    
    private String voucherStatus = "Pending";
    
    @Column
    private List<String> empId;
    
    @Column
    private List<String> designation;
	
	private LocalDate createdAt = LocalDate.now();
    
	private LocalDate approvedDate;

    private String activity;

    private LocalDate date;

    private String officeName;

    private String purchaseOrderType;
    
    private String poNo;
    
    
    

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PoReqTableData> tableData;

}
