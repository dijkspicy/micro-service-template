package com.huawei.cloudsop.xxx.dispatch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import com.huawei.bsp.common.HttpContext;
import com.huawei.cloudsop.xxx.common.XXXException;
import com.huawei.cloudsop.xxx.common.XXXResponse;
import com.huawei.cloudsop.xxx.common.errors.BadRequestException;

import static org.junit.Assert.*;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class BaseHandlerTest {

    @Test
    public void execute_without_concrete_generic_type() {
        Object out = new MockNoConcreteGenericTypeHandler().execute(mockHttpContext());
        assertNull(out);
        try {
            new MockNoConcreteGenericTypeHandler("error").execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must has super handler with concrete T"));
        }

        String out1 = new MockNoConcreteGenericTypeHandler<String>().execute(mockHttpContext());
        assertNull(out1);
        try {
            new MockNoConcreteGenericTypeHandler<String>("error").execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must has super handler with concrete T"));
        }

        Boolean out2 = new MockNoConcreteGenericTypeHandler<Boolean>(){}.execute(mockHttpContext());
        assertNull(out2);
        try {
            new MockNoConcreteGenericTypeHandler<Boolean>("error"){}.execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must has super handler with concrete T"));
        }
    }

    /**
     * directly extend BaseHandler with concrete generic type
     */
    @Test
    public void execute_directly() {
        String out = new MockDirectlyHandler().execute(mockHttpContext());
        assertEquals("OK", out);
        out = new MockDirectlyHandler("error").execute(mockHttpContext());
        assertEquals("", out);

        out = new MockTwiceHandler().execute(mockHttpContext());
        assertEquals("OK", out);
        out = new MockTwiceHandler("error").execute(mockHttpContext());
        assertEquals("", out);
    }

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
            new BaseHandler<MockResponse>() {
                @Override
                protected MockResponse doMainLogic(HttpContext context) throws XXXException {
                    throw new RuntimeException("123");
                }
            }.execute(mockHttpContext());
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Invalid generic response for handler:"));
        }
    }

    private static HttpContext mockHttpContext() {
        return new HttpContext();
    }

    static class MockTwiceHandler extends MockDirectlyHandler {
        public MockTwiceHandler(String error) {
            super(error);
        }

        public MockTwiceHandler() {
            this(null);
        }
    }

    static class MockDirectlyHandler extends BaseHandler<String> {

        private final String error;

        public MockDirectlyHandler(String error) {
            this.error = error;
        }

        public MockDirectlyHandler() {
            this(null);
        }

        @Override
        protected String doMainLogic(HttpContext context) throws XXXException {
            if (this.error != null) {
                throw new RuntimeException(this.error);
            }
            return "OK";
        }
    }

    static class MockNoConcreteGenericTypeHandler<T> extends BaseHandler<T> {
        private final String error;

        public MockNoConcreteGenericTypeHandler(String error) {
            this.error = error;
        }

        public MockNoConcreteGenericTypeHandler() {
            this(null);
        }

        @Override
        protected T doMainLogic(HttpContext context) throws XXXException {
            if (this.error != null) {
                throw new RuntimeException(this.error);
            }
            return null;
        }
    }

    static class MockResponse extends XXXResponse {
        public MockResponse(int a) {
        }
    }
}