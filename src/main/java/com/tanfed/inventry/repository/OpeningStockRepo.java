package com.tanfed.inventry.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.OpeningStock;
@Repository
public interface OpeningStockRepo extends JpaRepository<OpeningStock, Long> {

	public Optional<OpeningStock> findByObId(String obId);
	
	public List<OpeningStock> findByOfficeName(String officeName);
	
	public List<OpeningStock> findByOfficeNameAndProductNameAndAsOn(String officeName, String productName, LocalDate asOn);
	
	@Query("SELECT e FROM OpeningStock e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<OpeningStock> findPendingDataByOfficeName(@Param("officeName") String officeName);
	
	@Query("SELECT e FROM OpeningStock e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<OpeningStock> findApprovedDataByOfficeName(@Param("officeName") String officeName);
	
}
