package tool.common.web;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Configuration
public class WebConfiguration {

    /**
     * Content-Lengthに正常な値が設定されない問題を解決したかった.<br>
     * @return
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<ShallowEtagHeaderFilter>();
        filterRegistrationBean.setFilter(new ShallowEtagHeaderFilter());
        filterRegistrationBean.setUrlPatterns(Arrays.asList("*"));
        return filterRegistrationBean;
    }
}
