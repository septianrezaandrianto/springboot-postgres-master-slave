package com.demo.java_25_rnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class Java25RndApplication {

	public static void main(String[] args) {
		SpringApplication.run(Java25RndApplication.class, args);
	}

}
