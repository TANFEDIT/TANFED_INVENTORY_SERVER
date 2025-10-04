package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tanfed.inventry.entity.SalesReturn;

public interface SalesReturnRepo extends JpaRepository<SalesReturn, Long> {

	public List<SalesReturn> findByOfficeName(String officeName);
	
	public Optional<SalesReturn> findByGtnNo(String gtnNo);
	
	@Query("SELECT e FROM SalesReturn e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<SalesReturn> findPendingDataByOfficeName(@Param("officeName") String officeName);
	
	@Query("SELECT e FROM SalesReturn e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<SalesReturn> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
