package com.example.codecool.config;

import com.example.codecool.entity.source.JsonEntity;
import com.example.codecool.repository.source.JsonRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.codecool.repository.source",
        entityManagerFactoryRef = "datasource1EntityManagerFactory",
        transactionManagerRef = "datasource1TransactionManager"
)
public class DataSourceSourceDBConfig {


    @Bean(name = "datasource1")
    public DataSource datasource1() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:5432/sourceDB")
                .username("postgres")
                .password("admin")
                .build();
    }


    @Bean(name = "datasource1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean datasource2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("datasource1") DataSource dataSource) {
        val property = new HashMap<String, String>();
        property.put("hibernate.hbm2ddl.auto", "create-drop");
        property.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("com.example.codecool.entity.source")
                .persistenceUnit("datasource1")
                .properties(property)
                .build();
    }


    @Bean(name = "datasource1TransactionManager")
    public PlatformTransactionManager datasource2TransactionManager(
            @Qualifier("datasource1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Object loadDummyDataIntoSourceDB(JsonRepository jsonRepository) {
        List<JsonEntity> list = new ArrayList<>();
        list.add(new JsonEntity("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-01\", \"cancelled\": true, \"comment\": \"Foo was sick.\" }"));
        list.add(new JsonEntity("{ \"class\": \"Programming Basics\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-02-05\", \"cancelled\": false, \"success\": true, \"comment\": \"Everything was ok.\", \"results\": [{ \"dimension\": \"Coding\", \"result\": 80 }, { \"dimension\": \"Communication\", \"result\": 100}] }"));
        list.add(new JsonEntity("{ \"class\": \"Web Frameworks\", \"teacher\": \"bbb@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-05-11\", \"cancelled\": false, \"success\": false, \"comment\": \"Couldn''t really start, just wrote some HTML.\", \"results\": [{ \"dimension\": \"Coding\", \"result\": 0 }, {\"dimension\": \"HTML\", \"result\": 30}, {\"dimension\": \"Communication\", \"result\": 30}] }"));
        list.add(new JsonEntity("{ \"class\": \"Web Frameworks\", \"teacher\": \"aaa@codecool.com\", \"student\": \"foo@bar.com\", \"date\": \"2024-05-21\", \"cancelled\": false, \"success\": false, \"comment\": \"Wrote spaghetti code, and tried to sell it. Nice page, though.\", \"results\": [{ \"dimension\": \"Coding\", \"result\": 20 }, {\"dimension\": \"HTML\", \"result\": 100}, {\"dimension\": \"Communication\", \"result\": 80}] }"));
        jsonRepository.saveAll(list);
        return null;
    }
}
