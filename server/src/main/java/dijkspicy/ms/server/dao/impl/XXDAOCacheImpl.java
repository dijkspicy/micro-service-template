package dijkspicy.ms.server.dao.impl;

import com.google.inject.Singleton;

import static dijkspicy.ms.server.dao.XXDAO.XXCacheDAO;

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
