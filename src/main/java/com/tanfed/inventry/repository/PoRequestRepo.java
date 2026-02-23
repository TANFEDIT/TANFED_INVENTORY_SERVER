package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.PoRequest;

@Repository
public interface PoRequestRepo extends JpaRepository<PoRequest, Long> {

	public Optional<PoRequest> findByPoReqNo(String poReqNo);

	public List<PoRequest> findByOfficeName(String officeName);

	@Query("SELECT e FROM PoRequest e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<PoRequest> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM PoRequest e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<PoRequest> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
