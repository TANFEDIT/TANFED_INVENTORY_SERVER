package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.CheckMemoGoods;
@Repository
public interface CheckMemoGoodsRepo extends JpaRepository<CheckMemoGoods, Long> {

	public CheckMemoGoods findByCheckMemoNo(String checkMemoNo);
	
	@Query("SELECT e FROM CheckMemoGoods e WHERE e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified'")
	public List<CheckMemoGoods> findPendingData();
	
	@Query("SELECT e FROM CheckMemoGoods e WHERE e.voucherStatus = 'Approved'")
	public List<CheckMemoGoods> findApprovedData();
}
