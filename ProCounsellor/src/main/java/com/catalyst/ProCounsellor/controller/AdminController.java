package com.catalyst.ProCounsellor.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalyst.ProCounsellor.model.Admin;
import com.catalyst.ProCounsellor.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	
	@Autowired
    private AdminService adminService;
	
	@GetMapping("/{adminId}")
	public Admin getAdminById(@PathVariable String adminId) throws ExecutionException, InterruptedException {	
		return adminService.getAdminById(adminId);
	}

}
