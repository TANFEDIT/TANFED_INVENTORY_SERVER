package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.TermsPrice;
@Repository
public interface TermsPriceRepo extends JpaRepository<TermsPrice, Long> {

	public Optional<TermsPrice> findByTermsNo(String termsNo);

	public Optional<TermsPrice> findByCircularNo(String circularNo);
	
	@Query("SELECT e FROM TermsPrice e WHERE e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified'")
	public List<TermsPrice> findPendingData();
	
	@Query("SELECT e FROM TermsPrice e WHERE e.voucherStatus = 'Approved'")
	public List<TermsPrice> findApprovedData();
	
	@Query("SELECT e FROM TermsPrice e WHERE e.masterData.termsForMonth =:termsForMonth ")
	public List<TermsPrice> findByTermsForMonth(@Param("termsForMonth") String termsForMonth);
}
