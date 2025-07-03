package com.catalyst.ProCounsellor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
	    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
	})
public class ProCounsellorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProCounsellorApplication.class, args);
	}

}
