package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.TcBillEntryTempTable;

import jakarta.transaction.Transactional;

@Repository
public interface TcBillEntryTempTableRepo extends JpaRepository<TcBillEntryTempTable, Long> {

	public List<TcBillEntryTempTable> findByClaimBillNo(String claimBillNo);

	public List<TcBillEntryTempTable> findByIdNo(String idNo);

	@Modifying
	@Transactional
	@Query("DELETE FROM TcBillEntryTempTable i WHERE i.claimBillNo = :claimBillNo")
	public void deleteItemsByClaimBillNo(@Param("claimBillNo") String claimBillNo);
}
