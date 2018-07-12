package com.huawei.cloudsop.xxx.common;

import com.huawei.cloudsop.xxx.model.ODAEResponse;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/11
 */
public class XXXResponse extends ODAEResponse implements Returnable {
    private int retCode = OK.retCode;
    private String retInfo = OK.retInfo;

    @Override
    public int getRetCode() {
        return retCode;
    }

    @Override
    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    @Override
    public String getRetInfo() {
        return retInfo;
    }

    @Override
    public void setRetInfo(String retInfo) {
        this.retInfo = retInfo;
    }
}
