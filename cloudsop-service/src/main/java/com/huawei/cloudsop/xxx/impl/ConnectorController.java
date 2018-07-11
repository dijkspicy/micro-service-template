package com.huawei.cloudsop.xxx.impl;


import com.huawei.bsp.common.HttpContext;
import com.huawei.cloudsop.xxx.dispatch.connector.ConnectorHandler;

/**
 * ConnectorController
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public class ConnectorController {

    public String request(HttpContext context, String type) {
        return new ConnectorHandler(type).execute(context);
    }
}
