package tool.common.db.postgresql;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Configuration
@PropertySource(value = { "classpath:db-access.properties" })
@MapperScan(value = { "tool.common.db.postgresql.dao" }, sqlSessionFactoryRef = "postgresqlDbSqlSessionFactory")
public class PostgresqlDbAccessConfiguration {

    /**
     * 
     * @return
     */
    @Primary
    @Bean(name = { "postgresqlDbProperties" })
    @ConfigurationProperties(prefix = "postgresql.db.datasource.properties")
    public Properties properties() {
        return new Properties();
    }

    /**
     * 
     * @param properties
     * @return
     */
    @Primary
    @Bean(name = { "postgresqlDbDataSource" })
    public DataSource dataSource(@Qualifier(value = "postgresqlDbProperties") Properties properties) {
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String driverClassName = properties.getProperty("driver-class-name");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    /**
     * 
     * @param dataSource
     * @return
     */
    @Primary
    @Bean(name = { "postgresqlDbTransactionManager" })
    public PlatformTransactionManager transactionManager(
            @Qualifier(value = "postgresqlDbDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 
     * @param interceptorsProvider
     * @param properties
     * @param resourceLoader
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Primary
    @Bean(name = { "postgresqlDbSqlSessionFactory" })
    public SqlSessionFactory sqlSessionFactory(ObjectProvider<Interceptor[]> interceptorsProvider,
            MybatisProperties properties, ResourceLoader resourceLoader,
            @Qualifier(value = "postgresqlDbDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factoryBean.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        factoryBean.setDataSource(dataSource);
        factoryBean.setPlugins(interceptorsProvider.getIfAvailable());
        return factoryBean.getObject();
    }
}
