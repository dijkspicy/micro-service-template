package com.huawei.cloudsop.xxx.model;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class XXXResponse {
    private int retCode;
    private String retInfo;

    public int getRetCode() {
        return retCode;
    }

    public XXXResponse setRetCode(int retCode) {
        this.retCode = retCode;
        return this;
    }

    public String getRetInfo() {
        return retInfo;
    }

    public XXXResponse setRetInfo(String retInfo) {
        this.retInfo = retInfo;
        return this;
    }
}
