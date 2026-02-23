package com.tanfed.inventry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.Invoice;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Long> {

	public Optional<Invoice> findByInvoiceNo(String invoiceNo);

	public List<Invoice> findByOfficeName(String officeName);

	public List<Invoice> findByIcmNo(String icmNo);

	public List<Invoice> findByDespatchAdviceNo(String despatchAdviceNo);

	public List<Invoice> findByDcNo(String dcNo);

	public List<Invoice> findByActivityAndOfficeName(String activity, String officeName);

	@Query("SELECT e FROM Invoice e WHERE (e.voucherStatus = 'Pending' OR e.voucherStatus = 'Verified') AND e.officeName =:officeName")
	public List<Invoice> findPendingDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.voucherStatus = 'Approved' AND e.officeName =:officeName")
	public List<Invoice> findApprovedDataByOfficeName(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.ackEntryDate IS NOT NULL AND (e.voucherStatusICP1='Pending' OR e.voucherStatusICP1='Verified') AND e.officeName =:officeName")
	public List<Invoice> findICP1ByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.ackEntryDate IS NOT NULL AND e.voucherStatusICP1='Approved' AND e.officeName =:officeName")
	public List<Invoice> findICP1ApprovedByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.collectionMethod IS NOT NULL AND (e.voucherStatusICP2='Pending' OR e.voucherStatusICP2='Verified') AND e.officeName =:officeName")
	public List<Invoice> findICP2ByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.collectionMethod IS NOT NULL AND e.voucherStatusICP2='Approved' AND e.officeName =:officeName")
	public List<Invoice> findICP2ApprovedByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.dateOfPresent IS NOT NULL AND (e.voucherStatusICP3='Pending' OR e.voucherStatusICP3='Verified') AND e.officeName =:officeName")
	public List<Invoice> findICP3ByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.dateOfPresent IS NOT NULL AND e.voucherStatusICP3='Approved' AND e.officeName =:officeName")
	public List<Invoice> findICP3ApprovedByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.dateOfCollectionFromCcb IS NOT NULL AND (e.voucherStatusICP1='Pending' OR e.voucherStatusICP1='Verified') AND e.officeName =:officeName")
	public List<Invoice> findICP4ByStatus(@Param("officeName") String officeName);

	@Query("SELECT e FROM Invoice e WHERE e.dateOfCollectionFromCcb IS NOT NULL AND e.voucherStatusICP1='Approved' AND e.officeName =:officeName")
	public List<Invoice> findICP4ApprovedByStatus(@Param("officeName") String officeName);

}
