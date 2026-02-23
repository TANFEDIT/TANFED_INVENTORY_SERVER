package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.TcCheckMemo;

@Repository
public interface TcCheckMemoRepo extends JpaRepository<TcCheckMemo, Long> {

	public List<TcCheckMemo> findByOfficeName(String officeName);

	public TcCheckMemo findByCheckMemoNo(String checkMemoNo);

	@Query("SELECT e FROM TcCheckMemo e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<TcCheckMemo> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM TcCheckMemo e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<TcCheckMemo> findApprovedDataByOfficeName(@Param("officeName") String officeName);

}
