package org.example.runner;

import org.example.config.DatabaseMigrationProperties;
import org.example.service.GenericMigrationService;
import org.example.service.MigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MigrationsRunner implements CommandLineRunner {

    private final DatabaseMigrationProperties properties;

    public MigrationsRunner(DatabaseMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        List<MigrationService> migrationServices = new ArrayList<>();

        properties.getDatabases().forEach((dbName, dbProperties) -> {
            migrationServices.add(new GenericMigrationService(dbName, dbProperties));
        });

        migrationServices.forEach(MigrationService::runMigrations);
    }
}