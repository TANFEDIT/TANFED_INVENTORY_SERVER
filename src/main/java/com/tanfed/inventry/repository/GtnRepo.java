package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.GTN;

@Repository
public interface GtnRepo extends JpaRepository<GTN, Long> {

	public Optional<GTN> findByGtnNo(String gtnNo);

	public List<GTN> findByOfficeName(String officeName);

	public List<GTN> findByToRegion(String toRegion);

	public GTN findByIssuedGtnNo(String issuedGtnNo);

	@Query("SELECT e FROM GTN e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<GTN> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM GTN e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<GTN> findApprovedDataByOfficeName(@Param("officeName") String officeName);

}
