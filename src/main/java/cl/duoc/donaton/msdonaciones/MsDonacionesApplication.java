package cl.duoc.donaton.msdonaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsDonacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsDonacionesApplication.class, args);
    }
}
