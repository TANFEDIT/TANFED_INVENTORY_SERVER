package com.tanfed.inventry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanfed.inventry.entity.MpaEmployeeData;

@Repository
public interface MpaEmployeeDataRepo extends JpaRepository<MpaEmployeeData, Long> {

}
