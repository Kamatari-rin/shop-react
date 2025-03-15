package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DbMigrationsApplication implements CommandLineRunner {

    private final ApplicationContext context;

    public DbMigrationsApplication(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(DbMigrationsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }
}