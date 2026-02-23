package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.PurchaseBooking;

@Repository
public interface PurchaseBookingRepo extends JpaRepository<PurchaseBooking, Long> {

	public Optional<PurchaseBooking> findByCheckMemoNo(String checkMemoNo);

	public List<PurchaseBooking> findByPoNo(String poNo);

	public List<PurchaseBooking> findByActivity(String activity);

	@Query("SELECT e FROM PurchaseBooking e WHERE e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified'")
	public List<PurchaseBooking> findPendingData();

	@Query("SELECT e FROM PurchaseBooking e WHERE e.voucherStatus = 'Approved'")
	public List<PurchaseBooking> findApprovedData();
}
