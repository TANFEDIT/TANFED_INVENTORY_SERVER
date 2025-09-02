package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.GRN;


@Repository
public interface GrnRepo extends JpaRepository<GRN, Long> {

	public Optional<GRN> findByGrnNo(String grnNo);

	public GRN findByDcWdnRoNo(String dcWdnRoNo);
	
	public List<GRN> findByOfficeName(String officeName);

	public List<GRN> findByOfficeNameAndPoNo(String officeName, String poNo);
	
	@Query("SELECT e FROM GRN e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<GRN> findPendingDataByOfficeName(@Param("officeName") String officeName);
	
	@Query("SELECT e FROM GRN e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<GRN> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
