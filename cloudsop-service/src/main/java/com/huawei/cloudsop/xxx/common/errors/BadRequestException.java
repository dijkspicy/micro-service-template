package com.huawei.cloudsop.xxx.common.errors;


import com.huawei.cloudsop.xxx.common.XXXException;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/9
 */
public class BadRequestException extends XXXException {
    private static final long serialVersionUID = 1714337122516797732L;

    public BadRequestException(String msg) {
        super(BAD_REQUEST, msg);
    }

    public BadRequestException(String msg, Throwable e) {
        super(BAD_REQUEST, msg, e);
    }
}
