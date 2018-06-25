package dijkspicy.ms.server.persistence.impl;

import dijkspicy.ms.server.persistence.XXDAO;

import java.sql.SQLException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/24
 */
public class XXDAOImpl extends BaseDAOImpl implements XXDAO {
    @Override
    public Object get() throws SQLException {
        throw new SQLException("nothing");
    }
}
