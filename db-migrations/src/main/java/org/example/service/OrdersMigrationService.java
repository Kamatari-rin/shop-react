package org.example.service;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
@Service
public class OrdersMigrationService implements MigrationService {

    @Value("${db.migrations.orders.url}")
    private String url;

    @Value("${db.migrations.orders.username}")
    private String username;

    @Value("${db.migrations.orders.password}")
    private String password;

    @Value("${db.migrations.orders.changelog}")
    private String changelogFile;

    @Override
    public void runMigrations() {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changelogFile, new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
            System.out.println("Orders DB migrations applied successfully.");
        } catch (SQLException | LiquibaseException e) {
            e.printStackTrace();
            throw new RuntimeException("Error applying Orders DB migrations", e);
        }
    }
}