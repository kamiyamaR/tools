package tool.common.db.mysql;

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
@MapperScan(value = { "tool.common.db.mysql.dao" }, sqlSessionFactoryRef = "mysqlDbSqlSessionFactory")
public class MysqlDbAccessConfiguration {

    /**
     * 
     * @return
     */
    @Primary
    @Bean(name = { "mysqlDbProperties" })
    @ConfigurationProperties(prefix = "mysql.db.datasource.properties")
    public Properties properties() {
        return new Properties();
    }

    /**
     * 
     * @param properties
     * @return
     */
    //@Primary
    @Bean(name = { "mysqlDbDataSource" })
    public DataSource dataSource(@Qualifier(value = "mysqlDbProperties") Properties properties) {
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
    //@Primary
    @Bean(name = { "mysqlDbTransactionManager" })
    public PlatformTransactionManager transactionManager(
            @Qualifier(value = "mysqlDbDataSource") DataSource dataSource) {
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
    //@Primary
    @Bean(name = { "mysqlDbSqlSessionFactory" })
    public SqlSessionFactory sqlSessionFactory(ObjectProvider<Interceptor[]> interceptorsProvider,
            MybatisProperties properties, ResourceLoader resourceLoader,
            @Qualifier(value = "mysqlDbDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factoryBean.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        factoryBean.setDataSource(dataSource);
        factoryBean.setPlugins(interceptorsProvider.getIfAvailable());
        return factoryBean.getObject();
    }
}
