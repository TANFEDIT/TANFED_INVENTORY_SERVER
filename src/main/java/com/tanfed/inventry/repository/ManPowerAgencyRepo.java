package com.tanfed.inventry.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.ManPowerAgency;

@Repository
public interface ManPowerAgencyRepo extends JpaRepository<ManPowerAgency, Long> {

	public List<ManPowerAgency> findByOfficeName(String officeName);

	public ManPowerAgency findByContractFirm(String contractFirm);
}
