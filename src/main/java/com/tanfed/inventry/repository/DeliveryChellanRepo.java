package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.DeliveryChellan;

@Repository
public interface DeliveryChellanRepo extends JpaRepository<DeliveryChellan, Long> {

	public Optional<DeliveryChellan> findByDcNo(String dcNo);

	public List<DeliveryChellan> findByDespatchAdviceNo(String despatchAdviceNo);

	public List<DeliveryChellan> findByClNo(String clNo);

	public List<DeliveryChellan> findByOfficeName(String officeName);

	@Query("SELECT e FROM DeliveryChellan e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<DeliveryChellan> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM DeliveryChellan e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<DeliveryChellan> findApprovedDataByOfficeName(@Param("officeName") String officeName);
}
