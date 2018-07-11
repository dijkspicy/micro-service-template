package com.huawei.cloudsop.xxx.dispatch;

import com.huawei.bsp.common.HttpContext;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.common.XXXResponse;
import com.huawei.cloudsop.xxx.common.errors.BadRequestException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class BaseHandlerTest {

    @Test
    public void execute_anonymous() {
        Object out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpContext context) throws XXXException {
                return 1;
            }
        }.execute(mockHttpContext());
        assertEquals(Integer.class, out.getClass());
        assertEquals(1, out);

        out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpContext context) throws XXXException {
                throw new BadRequestException("123");
            }
        }.execute(mockHttpContext());
        assertEquals(XXXResponse.class, out.getClass());

        out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals(XXXResponse.class, out.getClass());

        out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpContext context) throws XXXException {
                throw new Error("123");
            }
        }.execute(mockHttpContext());
        assertEquals(XXXResponse.class, out.getClass());
    }

    @Test
    public void execute_anonymous_with_t() {
        String out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpContext context) throws XXXException {
                return "123";
            }
        }.execute(mockHttpContext());
        assertEquals("123", out);

        Boolean out2 = new BaseHandler<Boolean>() {
            @Override
            protected Boolean doMainLogic(HttpContext context) throws XXXException {
                return false;
            }
        }.execute(mockHttpContext());
        assertEquals(false, out2);

        XXXResponse out3 = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
                return new XXXResponse();
            }
        }.execute(mockHttpContext());
        assertEquals(0, out3.getRetCode());
        assertEquals("OK", out3.getRetInfo());

        out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("", out);

        out3 = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals(-2, out3.getRetCode());
        assertTrue(out3.getRetInfo().startsWith("[Internal Server Error] Unknown exception"));
    }

    @Test
    public void execute_with_interface() {
        Object out = new BaseHandler<Map<String, Object>>() {
            @Override
            protected Map<String, Object> doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("{}", out.toString());

        out = new BaseHandler<Collection<Object>>() {
            @Override
            protected Collection<Object> doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("[]", out.toString());

        out = new BaseHandler<byte[]>() {
            @Override
            protected byte[] doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("[]", Arrays.toString((byte[]) out));

        out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("", out);

        out = new BaseHandler<Boolean>() {
            @Override
            protected Boolean doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals(false, out);

        out = new BaseHandler<Integer>() {
            @Override
            protected Integer doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals(0, out);

        out = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpContext context) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(mockHttpContext());
        assertEquals("[Internal Server Error] Unknown exception: 123", ((XXXResponse) out).getRetInfo());
    }

    private static HttpContext mockHttpContext() {
        return new HttpContext();
    }

    static abstract class MockHandler<T> extends BaseHandler<T> {

    }

    static abstract class Mock2Handler<R, T> extends BaseHandler<T> {

    }
}