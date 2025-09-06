package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tanfed.inventry.model.Office;


@FeignClient(name = "USER-SERVICE", url = "${USER_SERVICE_URL}")
public interface UserService {
	
	@GetMapping("/auth/getofficelist")
	public List<Office> getOfficeList();
	
	@GetMapping("/auth/blocklist")
	public List<String> getBlockedJwtList();
	
	@GetMapping("/auth/fetchuserdesignation/{empId}")
	public String getNewDesignation(@PathVariable String empId);
}
