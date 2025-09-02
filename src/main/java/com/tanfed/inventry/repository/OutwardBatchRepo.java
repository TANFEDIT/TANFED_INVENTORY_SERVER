package com.tanfed.inventry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.OutwardBatch;

import jakarta.transaction.Transactional;
@Repository
public interface OutwardBatchRepo extends JpaRepository<OutwardBatch, Long> {

	@Modifying
	@Transactional
	@Query("DELETE FROM OutwardBatch i WHERE i.dcNo = :dcNo")
	public void deleteItemsByDcNo(@Param("dcNo") String dcNo);

}
