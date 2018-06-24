package dijkspicy.ms.server.persistence;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
@Component
public class DAO implements ApplicationContextAware {

    public static <T> T getInstance(Class<T> beanClazz) {
        try {
            ApplicationContext applicationContext = Optional.ofNullable(Holder.applicationContext)
                    .orElseThrow(() -> new ApplicationContextException("You may forget to configure BeanHelper as a bean"));

            Map<String, T> names = applicationContext.getBeansOfType(beanClazz);
            if (names.isEmpty()) {
                throw new DAOException("No implementation of " + beanClazz.getName());
            }
            return names.values().iterator().next();
        } catch (BeansException e) {
            throw new DAOException("You may forget to configure bean of " + beanClazz.getName());
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
