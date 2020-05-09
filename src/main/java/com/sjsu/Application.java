package com.sjsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan({ "com.sjsu.drive*" })
public class Application {

    public static void main(String[] args) {
        System.out.println("Inside Application class - Cloud Project");
        SpringApplication.run(Application.class, args);

    }
}
