package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.MpaCheckMemo;

@Repository
public interface MpaCheckMemoRepo extends JpaRepository<MpaCheckMemo, Long> {

	public List<MpaCheckMemo> findByOfficeName(String officeName);

	public MpaCheckMemo findByCheckMemoNo(String checkMemoNo);

	@Query("SELECT e FROM MpaCheckMemo e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<MpaCheckMemo> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM MpaCheckMemo e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<MpaCheckMemo> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
