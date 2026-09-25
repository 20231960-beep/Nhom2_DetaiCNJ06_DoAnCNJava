package com.university.admission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Diem khoi dau cua ung dung Spring Boot.
 * Chay class nay (Run) trong NetBeans de khoi dong server tich hop Tomcat tren port 8080.
 */
@SpringBootApplication
public class AdmissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmissionApplication.class, args);
    }

}
