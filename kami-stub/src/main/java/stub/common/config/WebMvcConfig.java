package stub.common.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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

}
