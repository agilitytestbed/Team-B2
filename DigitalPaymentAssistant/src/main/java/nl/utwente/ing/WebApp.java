package nl.utwente.ing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import nl.utwente.ing.transaction.DatabaseCommunication;

@SpringBootApplication
public class WebApp {
	
	public static void main(String[] args) {
		DatabaseCommunication.generateTables();
		SpringApplication.run(WebApp.class, args);
	}
}
