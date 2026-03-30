package com.example.usermangment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 👈 AJOUTER ÇA

@SpringBootApplication
@EnableScheduling // utilisation des taches automatique
public class UsermangmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsermangmentApplication.class, args);
	}
}