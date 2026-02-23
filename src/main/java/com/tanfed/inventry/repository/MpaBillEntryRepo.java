package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.MpaBillEntry;

@Repository
public interface MpaBillEntryRepo extends JpaRepository<MpaBillEntry, Long> {

	public Optional<MpaBillEntry> findByCheckMemoNo(String checkMemoNo);

	public List<MpaBillEntry> findByOfficeName(String officeName);

	@Query("SELECT e FROM MpaBillEntry e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<MpaBillEntry> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM MpaBillEntry e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<MpaBillEntry> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
