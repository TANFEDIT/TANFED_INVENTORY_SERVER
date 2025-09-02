package com.tanfed.inventry.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DespatchAdviceTableDataEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String productName;
	
	@Column
	private String productCategory;
	
	@Column
	private String productGroup;
	
	@Column
	private String packing;
	
	@Column
	private Double qty;
	
	@Column
	private Double qtyAvlForDc;
	
    @PrePersist
    protected void onCreate() {
        this.qtyAvlForDc = qty;
    }
	
}
