package lk.ijse.dep11.app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.SQLException;


@Configuration
@EnableWebMvc
@ComponentScan
@PropertySource("classpath:/application.properties")
public class WebAppConfig {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(){
        return new MethodValidationPostProcessor();
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource pool(Environment env) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getRequiredProperty("app.datasource.drive_class_name"));
        config.setJdbcUrl(env.getRequiredProperty("app.datasource.url"));
        config.setUsername(env.getRequiredProperty("app.datasource.username"));
        config.setPassword(env.getRequiredProperty("app.datasource.password"));
        config.setMaximumPoolSize(env.getRequiredProperty("app.datasource.max_pool_size", Integer.class));
        return new HikariDataSource(config);
    }
}
