package tool.common.web;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AfterBeanCheck implements InitializingBean {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> beanMap = this.applicationContext.getBeansOfType(Object.class);
        if (Objects.nonNull(beanMap)) {
            log.info("↓DIコンテナに登録されているBeanたち.");
            for (Entry<String, Object> beanEntry : beanMap.entrySet()) {
                String beanName = beanEntry.getKey();
                Object bean = beanEntry.getValue();
                log.info("beanName=[{}]/bean=[{}]", beanName, bean);
            }
            log.info("");
        }
    }
}
