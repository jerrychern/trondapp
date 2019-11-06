package shop.tronlucky.trondapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import shop.tronlucky.trondapp.config.Args;

@SpringBootApplication
@ServletComponentScan
public class TronDApp {
    public static void main(String[] args) {
        Args.getInstance().setParam(args);
        SpringApplication.run(TronDApp.class, args);
    }
}