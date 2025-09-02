package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.PoReqTempTable;

import jakarta.transaction.Transactional;
@Repository
public interface PoReqTempTableRepo extends JpaRepository<PoReqTempTable, Long> {

	public List<PoReqTempTable> findByOfficeName(String officeName);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM PoReqTempTable i WHERE i.productName = :productName AND i.poReqNo = :poReqNo")
	public void deleteItemsByProductNameAndPoReqNo(@Param("productName") String productName,
	                                        @Param("poReqNo") String poReqNo);

}
