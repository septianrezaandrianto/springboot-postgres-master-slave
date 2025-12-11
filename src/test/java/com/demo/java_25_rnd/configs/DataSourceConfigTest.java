package com.demo.java_25_rnd.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigTest {

    static class DummyDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getLogger("DummyDataSource");
        }
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void whenReadOnlyTransaction_thenRoutingKeyIsREAD() throws Exception {
        DataSourceConfig config = new DataSourceConfig();
        DataSource writeDs = new DummyDataSource();
        DataSource readDs = new DummyDataSource();

        AbstractRoutingDataSource routing =
                (AbstractRoutingDataSource) config.routingDataSource(writeDs, readDs);

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

        Object key = invokeDetermineCurrentLookupKey(routing);

        assertEquals("READ", key, "readOnly transaction should route to READ datasource");
    }

    @Test
    void whenWriteTransaction_thenRoutingKeyIsWRITE() throws Exception {
        DataSourceConfig config = new DataSourceConfig();
        DataSource writeDs = new DummyDataSource();
        DataSource readDs = new DummyDataSource();

        AbstractRoutingDataSource routing =
                (AbstractRoutingDataSource) config.routingDataSource(writeDs, readDs);

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);

        Object key = invokeDetermineCurrentLookupKey(routing);

        assertEquals("WRITE", key, "non-readOnly transaction should route to WRITE datasource");
    }

    @Test
    void whenNoTransaction_thenRoutingKeyIsWRITE() throws Exception {
        DataSourceConfig config = new DataSourceConfig();
        DataSource writeDs = new DummyDataSource();
        DataSource readDs = new DummyDataSource();

        AbstractRoutingDataSource routing =
                (AbstractRoutingDataSource) config.routingDataSource(writeDs, readDs);

        Object key = invokeDetermineCurrentLookupKey(routing);

        assertEquals("WRITE", key, "without transaction it should fall back to WRITE datasource");
    }

    @Test
    void dataSource_shouldWrapRoutingWithLazyProxy() {
        DataSourceConfig config = new DataSourceConfig();
        DataSource writeDs = new DummyDataSource();
        DataSource readDs = new DummyDataSource();

        DataSource routing = config.routingDataSource(writeDs, readDs);
        DataSource ds = config.dataSource(routing);

        assertInstanceOf(org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy.class, ds);
    }

    @Test
    void entityManagerFactory_shouldUseGivenDataSource() {
        DataSourceConfig config = new DataSourceConfig();
        DataSource dummy = new DummyDataSource();

        LocalContainerEntityManagerFactoryBean emf = config.entityManagerFactory(dummy);
        assertNotNull(emf);
    }


    private Object invokeDetermineCurrentLookupKey(AbstractRoutingDataSource routing) throws Exception {
        Method m = routing.getClass().getDeclaredMethod("determineCurrentLookupKey");
        m.setAccessible(true);
        return m.invoke(routing);
    }
}
