package com.demo.java_25_rnd.configs;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.write")
    public DataSourceProperties writeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name="writeDataSource")
    public DataSource writeDataSource(DataSourceProperties writeDataSourceProperties) {
        return writeDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name="readDataSource")
    public DataSource readDataSource(DataSourceProperties readDataSourceProperties) {
        return readDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(name="routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("writeDataSource") DataSource writeDsp,
            @Qualifier("readDataSource") DataSource readDsp
    ) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put("WRITE", writeDsp);
        targets.put("READ", readDsp);

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                boolean active = TransactionSynchronizationManager.isActualTransactionActive();
                boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                String txName = TransactionSynchronizationManager.getCurrentTransactionName();
                String threadName = Thread.currentThread().getName();

                log.info(">> TX name = {}, active = {}, readOnly = {}, thread = {}",
                        txName, active, readOnly, threadName);

                String key = readOnly ? "READ" : "WRITE";
                log.info(">> Routing to {} datasource (transaction readOnly = {})", key, readOnly);
                return key;
            }
        };

        routing.setDefaultTargetDataSource(writeDsp);
        routing.setTargetDataSources(targets);
        routing.afterPropertiesSet();
        return routing;
    }

    @Primary
    @Bean(name="dataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
            return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("dataSource") DataSource ds) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.demo.java_25_rnd");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return emf;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}
