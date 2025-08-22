package dev.bermeb.expensevault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ExpenseVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseVaultApplication.class, args);
    }

}
