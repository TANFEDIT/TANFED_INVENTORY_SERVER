package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tanfed.inventry.entity.SupplierInvoiceDetails;

public interface SupplierInvoiceDetailsRepo extends JpaRepository<SupplierInvoiceDetails, Long> {

	public SupplierInvoiceDetails findByInvoiceNumber(String invoiceNumber);
	
	public List<SupplierInvoiceDetails> findByMonthOfSupply(String monthOfSupply);
	
	@Query("SELECT e FROM SupplierInvoiceDetails e WHERE e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified'")
	public List<SupplierInvoiceDetails> findPendingData();
	
	@Query("SELECT e FROM SupplierInvoiceDetails e WHERE e.voucherStatus = 'Approved'")
	public List<SupplierInvoiceDetails> findApprovedData();
}
