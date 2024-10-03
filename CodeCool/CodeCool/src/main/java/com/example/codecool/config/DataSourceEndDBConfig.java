package com.example.codecool.config;

import com.example.codecool.entity.end.UserEntity;
import com.example.codecool.repository.end.UserRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.codecool.repository.end",
        entityManagerFactoryRef = "datasource2EntityManagerFactory",
        transactionManagerRef = "datasource2TransactionManager"
)
public class DataSourceEndDBConfig {

    @Primary
    @Bean(name = "datasource2")
    public DataSource datasource2() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:5432/endDB")
                .username("postgres")
                .password("admin")
                .build();
    }

    @Primary
    @Bean(name = "datasource2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean datasource2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("datasource2") DataSource dataSource) {
        val property = new HashMap<String, String>();
        property.put("hibernate.hbm2ddl.auto", "create-drop");
        property.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("com.example.codecool.entity.end")
                .persistenceUnit("datasource2")
                .properties(property)
                .build();
    }

    @Primary
    @Bean(name = "datasource2TransactionManager")
    public PlatformTransactionManager datasource2TransactionManager(
            @Qualifier("datasource2EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


    @Bean
    public Object loadDummyDataIntoEndDB(UserRepository userRepository) {
        List<UserEntity> list = new ArrayList<>();
        list.add(new UserEntity("Mr. Foo", "foo@bar.com", LocalDate.of(1998, 5, 4), true));
        list.add(new UserEntity("Mr. aaa", "aaa@codecool.com", LocalDate.of(1981, 7, 8), false));
        list.add(new UserEntity("Mrs. bbb", "bbb@codecool.com", LocalDate.of(1985, 10, 25), false));
        userRepository.saveAll(list);

        return null;
    }
}
