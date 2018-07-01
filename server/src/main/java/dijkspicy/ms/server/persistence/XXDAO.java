package dijkspicy.ms.server.persistence;

import com.google.inject.ImplementedBy;
import dijkspicy.ms.server.persistence.impl.XXDAOCacheImpl;
import dijkspicy.ms.server.persistence.impl.XXDAOImpl;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/24
 */
@ImplementedBy(XXDAOImpl.class)
public interface XXDAO {
    Object get();

    @ImplementedBy(XXDAOCacheImpl.class)
    public interface XXCacheDAO extends XXDAO {
    }
}
