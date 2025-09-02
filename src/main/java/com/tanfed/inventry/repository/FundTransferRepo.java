package com.tanfed.inventry.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.FundTransfer;
@Repository
public interface FundTransferRepo extends JpaRepository<FundTransfer, Long> {

	public List<FundTransfer> findByOfficeName(String officeName);
	
	public List<FundTransfer> findByAccountNo(Long accountNo);

	public List<FundTransfer> findByToAccountNo(Long toAccountNo);
	
	public List<FundTransfer> findByDate(LocalDate date);
	
	@Query("SELECT e FROM FundTransfer e WHERE (e.voucherStatus='Pending' OR e.voucherStatus='Verified') AND e.activity IS NOT NULL AND e.officeName =:officeName")
	public List<FundTransfer> findICP5ByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM FundTransfer e WHERE e.voucherStatus='Approved' AND e.activity IS NOT NULL AND e.officeName =:officeName")
	public List<FundTransfer> findICP5ApprovedByStatus(@Param("officeName") String officeName);
}
