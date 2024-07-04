package stub.common.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebMvcConfigurerEx implements WebMvcConfigurer {

    /*
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable(); // デフォルトサーブレットへの転送機能を有効化
    }
    */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 「/resources/**」配下へのアクセスを受けた際にはクラスパス配下の「/static/**」から該当リソースを見つける
        registry.addResourceHandler("/view/**").addResourceLocations("classpath:/*");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("WebMvcConfig::configureMessageConverters() call.");
        converters.forEach(converter -> log.info("  converter=[{}], class=[{}]", converter, converter.getClass()));
    }

    public static class MappingJackson2HttpMessageConverterEx extends MappingJackson2HttpMessageConverter {

        public MappingJackson2HttpMessageConverterEx() {
            super();
        }

        @Override
        protected Long getContentLength(Object retValue, @Nullable MediaType contentType) throws IOException {
            return null;
        }
    }

    public static class WebMvcConfigurationSupportEx extends WebMvcConfigurationSupport {
        public List<HttpMessageConverter<?>> defaultMessageConverters() {
            return super.getMessageConverters();
        }
    }

}
