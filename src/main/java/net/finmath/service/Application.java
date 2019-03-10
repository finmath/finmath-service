/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christianfries.com.
 *
 * Created on 15 Oct 2018
 */

package net.finmath.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring-boot application entry point (this will start the RESTful server an do a lot of stuff just from the annotations).
 * 
 * @author Christian Fries
 */
@SpringBootApplication
public class Application {

	/**
	 * Application entry point.
	 * 
	 * @param args Program arguments (not used).
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
