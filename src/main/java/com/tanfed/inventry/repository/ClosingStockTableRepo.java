package com.tanfed.inventry.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.ClosingStockTable;

@Repository

public interface ClosingStockTableRepo extends JpaRepository<ClosingStockTable, Long> {

	public List<ClosingStockTable> findByOfficeNameAndProductName(String officeName, String productName);

	public ClosingStockTable findByOfficeNameAndProductNameAndDateAndGodownName(String officeName, String productName,
			LocalDate date, String godownName);
}
