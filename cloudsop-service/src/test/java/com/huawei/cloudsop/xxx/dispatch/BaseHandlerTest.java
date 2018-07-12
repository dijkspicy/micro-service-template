package com.huawei.cloudsop.xxx.dispatch;

import com.huawei.bsp.common.HttpContext;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.common.XXXResponse;
import com.huawei.cloudsop.xxx.common.errors.BadRequestException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class BaseHandlerTest {

    /**
     * anonymous handler will return {@link XXXResponse} when exception
     */
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

    /**
     * anonymous handler know its concrete generic class
     */
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

    /**
     * anonymous handler with some normal interface/simple type will return its right type when exception
     */
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

    @Test
    public void execute_cannt_instance() {
        try {
            new BaseHandler<MockResposne>() {
                @Override
                protected MockResposne doMainLogic(HttpContext context) throws XXXException {
                    throw new RuntimeException("123");
                }
            }.execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Invalid generic response for handler:"));
        }
    }

    @Test
    public void execute_not_extend_two() {
        try {
            new MockHandlerWithError<Boolean>() {
            }.execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must extend BaseHandler:"));
        }
    }

    @Test
    public void execute_not_extends_a_class() {
        try {
            new MockHandlerWithError() {
            }.execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler without actual type information:"));
        }
    }

    private static HttpContext mockHttpContext() {
        return new HttpContext();
    }

    static class MockHandlerWithError<T> extends BaseHandler<T> {

        @Override
        protected T doMainLogic(HttpContext context) throws XXXException {
            throw new RuntimeException("123");
        }
    }

    static class MockResposne extends XXXResponse {
        public MockResposne(int a) {
        }
    }
}