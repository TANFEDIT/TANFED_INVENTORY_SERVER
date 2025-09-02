package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.DespatchAdvice;
@Repository
public interface DespatchAdviceRepo extends JpaRepository<DespatchAdvice, Long> {

	public Optional<DespatchAdvice> findByDespatchAdviceNo(String despatchAdviceNo);
	
	public List<DespatchAdvice> findByOfficeName(String officeName);
	
	public List<DespatchAdvice> findByActivity(String activity);
		
	@Query("SELECT e FROM DespatchAdvice e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified')  AND e.officeName =:officeName")
	public List<DespatchAdvice> findPendingDataByOfficeName(@Param("officeName") String officeName);
	
	@Query("SELECT e FROM DespatchAdvice e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<DespatchAdvice> findApprovedDataByOfficeName(@Param("officeName") String officeName);
	
}