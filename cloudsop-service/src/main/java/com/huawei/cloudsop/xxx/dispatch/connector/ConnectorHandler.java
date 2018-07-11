package com.huawei.cloudsop.xxx.dispatch.connector;

import com.huawei.cloudsop.common.HttpContext;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.dispatch.BaseHandler;
import com.huawei.cloudsop.xxx.model.XXXResponse;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class ConnectorHandler extends BaseHandler<XXXResponse> {
    public ConnectorHandler(String type) {
    }

    @Override
    protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
        return null;
    }
}
