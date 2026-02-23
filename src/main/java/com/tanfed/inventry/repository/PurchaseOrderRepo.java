package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.PurchaseOrder;

@Repository
public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Long> {

	public Optional<PurchaseOrder> findByPoNo(String poNo);

	@Query("SELECT e FROM PurchaseOrder e WHERE e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified'")
	public List<PurchaseOrder> findPendingData();

	@Query("SELECT e FROM PurchaseOrder e WHERE e.voucherStatus = 'Approved'")
	public List<PurchaseOrder> findApprovedData();

//	@Query("SELECT e FROM PurchaseOrder e WHERE e.sobVoucherStatus = 'Pending' OR e.sobVoucherStatus = 'Verified'")
//	public List<PurchaseOrder> findSobPendingData();
//	
//	@Query("SELECT e FROM PurchaseOrder e WHERE e.sobVoucherStatus = 'Approved'")
//	public List<PurchaseOrder> findSobApprovedData();
}
