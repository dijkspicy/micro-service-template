package dijkspicy.ms.server.dao;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import dijkspicy.ms.server.dao.impl.XXDAOCacheImpl;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/1
 */
public class XXDAOTest {

    @Test
    public void get() throws SQLException {
        XXDAO xxDAO = Guice.createInjector(binder -> {
            binder.bind(XXDAO.class).annotatedWith(Names.named("xxCacheDAO")).to(XXDAOCacheImpl.class);
        })
                .getInstance(XXDAO.class);
        System.out.println(xxDAO.get());

        Map<Key<?>, Binding<?>> bindings = Guice.createInjector(binder -> {
            binder.bind(XXDAO.class).annotatedWith(Names.named("xxCacheDAO")).to(XXDAOCacheImpl.class);
        })
                .getBindings();
        bindings.forEach(((key, binding) ->{
            System.out.println(key);
            System.out.println(binding.getKey());
        }));
    }
}