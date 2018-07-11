package com.huawei.cloudsop.xxx.dispatch;

import com.huawei.cloudsop.common.HttpContext;
import com.huawei.cloudsop.xxx.common.Timer;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.model.XXXResponse;
import org.junit.Test;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class BaseHandlerTest {

    @Test
    public void execute_no_generic_type() {
        new BaseHandler() {
            @Override
            protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
                return null;
            }
        };
    }

    @Test
    public void exeucte() {
        new BaseHandler<AbstractXXXResponse>() {
            @Override
            protected AbstractXXXResponse doMainLogic(HttpContext context) throws XXXException {
                return null;
            }
        };
    }

    @Test
    public void execute() {
        try (final Timer i = Timer.start(1)) {
            new TestHandler();
        }

        try (final Timer i = Timer.start(2)) {
            new TestHandler();
        }
    }

    static class TestHandler extends BaseHandler<XXXResponse> {

        @Override
        protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
            return null;
        }
    }

    static abstract class AbstractXXXResponse extends XXXResponse {
    }
}