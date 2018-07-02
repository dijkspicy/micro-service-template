package dijkspicy.ms.server.dao.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dijkspicy.ms.server.dao.XXDAO;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/24
 */
@Singleton
public class XXDAOImpl extends BaseDAOImpl implements XXDAO {

    @Inject
    private XXCacheDAO xxCacheDAO;

    @Override
    public Object get() {
        return xxCacheDAO.get();
    }
}
