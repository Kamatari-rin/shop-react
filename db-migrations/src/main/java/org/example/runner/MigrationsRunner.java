package org.example.runner;

import org.example.service.MigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MigrationsRunner implements CommandLineRunner {

    private final List<MigrationService> migrationServices;

    public MigrationsRunner(List<MigrationService> migrationServices) {
        this.migrationServices = migrationServices;
    }

    @Override
    public void run(String... args) {
        migrationServices.forEach(MigrationService::runMigrations);
    }
}