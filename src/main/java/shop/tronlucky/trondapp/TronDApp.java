package shop.tronlucky.trondapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class TronDApp {
    public static void main(String[] args) {
        SpringApplication.run(TronDApp.class, args);
        System.out.println("hello springboot!");
    }
}
