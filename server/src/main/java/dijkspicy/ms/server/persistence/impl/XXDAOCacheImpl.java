package dijkspicy.ms.server.persistence.impl;

import com.google.inject.Singleton;

import static dijkspicy.ms.server.persistence.XXDAO.XXCacheDAO;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/1
 */
@Singleton
public class XXDAOCacheImpl implements XXCacheDAO {
    @Override
    public Object get() {
        return 1;
    }
}
