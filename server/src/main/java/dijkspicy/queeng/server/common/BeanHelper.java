package dijkspicy.queeng.server.common;

import java.util.Optional;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

/**
 * BeanHelper
 *
 * @author dijkspicy
 * @date 2018/6/19
 */
@Component
public class BeanHelper implements ApplicationContextAware {

    public <T> T getBean(Class<T> beanClazz) {
        try {
            return Optional.ofNullable(Holder.applicationContext)
                    .orElseThrow(() -> new ApplicationContextException("You may forget to configure BeanHelper as a bean"))
                    .getBean(beanClazz);
        } catch (BeansException e) {
            throw new NoSuchBeanDefinitionException(beanClazz, "You may forget to configure bean of " + beanClazz.getName());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Holder.applicationContext = applicationContext;
    }

    private static class Holder {
        static ApplicationContext applicationContext;
    }
}
