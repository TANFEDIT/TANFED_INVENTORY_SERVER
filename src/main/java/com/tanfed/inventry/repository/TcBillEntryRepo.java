package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.TcBillEntry;
@Repository
public interface TcBillEntryRepo extends JpaRepository<TcBillEntry, Long> {

	public Optional<TcBillEntry> findByCheckMemoNo(String checkMemoNo);
	
	public List<TcBillEntry> findByOfficeName(String officeName);

	public TcBillEntry findByClaimBillNo(String claimBillNo);
	
	@Query("SELECT e FROM TcBillEntry e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<TcBillEntry> findPendingDataByOfficeName(@Param("officeName") String officeName);
	
	@Query("SELECT e FROM TcBillEntry e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<TcBillEntry> findApprovedDataByOfficeName(@Param("officeName") String officeName);
	

}
