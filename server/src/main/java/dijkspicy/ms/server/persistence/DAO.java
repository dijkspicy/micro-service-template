package dijkspicy.ms.server.persistence;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
public abstract class DAO {

    private static ApplicationContext applicationContext;

    public static <T> T getInstance(Class<T> beanClazz) {
        try {
            ApplicationContext applicationContext = Optional.ofNullable(DAO.applicationContext)
                    .orElseThrow(() -> new ApplicationContextException("You may forget to configure DAO as a bean"));

            Map<String, T> names = applicationContext.getBeansOfType(beanClazz);
            if (names.isEmpty()) {
                throw new DAOException("No implementation of " + beanClazz.getName());
            }
            return names.values().iterator().next();
        } catch (BeansException e) {
            throw new DAOException("You may forget to configure bean of " + beanClazz.getName(), e);
        }
    }

    @Component
    private static class Holder {
        @Autowired
        private Holder(ApplicationContext applicationContext) {
            DAO.applicationContext = applicationContext;
        }
    }
}
