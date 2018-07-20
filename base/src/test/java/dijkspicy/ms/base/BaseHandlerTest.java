package dijkspicy.ms.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import dijkspicy.ms.base.errors.BadRequestException;
import mockit.Injectable;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class BaseHandlerTest {

    @Injectable
    private HttpServletRequest request;
    @Injectable
    private HttpServletResponse response;

    static void print(Returnable returnable) {
        try {
            System.out.println(Returnable.RET_MAPPER.writeValueAsString(returnable));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void execute_not_returnable_simple() {
        NormalObject out = new BaseHandler<NormalObject>() {
            @Override
            protected NormalObject doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_returnable_final() {
        FinalResponse out = new BaseHandler<FinalResponse>() {
            @Override
            protected FinalResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }

    @Test
    public void execute_returnable_map() {
        MapResponse out = new BaseHandler<MapResponse>() {
            @Override
            protected MapResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }

    @Test
    public void execute_returnable_collection() {
        CollectionResponse out = new BaseHandler<CollectionResponse>() {
            @Override
            protected CollectionResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }

    @Test
    public void execute_without_concrete_generic_type() {
        Object out = new MockNoConcreteGenericTypeHandler().execute(request, response);
        assertNull(out);
        try {
            new MockNoConcreteGenericTypeHandler("error").execute(request, response);
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must has super handler with concrete T"));
        }

        String out1 = new MockNoConcreteGenericTypeHandler<String>().execute(request, response);
        assertNull(out1);
        try {
            new MockNoConcreteGenericTypeHandler<String>("error").execute(request, response);
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Concrete handler must has super handler with concrete T"));
        }

        Boolean out2 = new MockNoConcreteGenericTypeHandler<Boolean>() {
        }.execute(request, response);
        assertNull(out2);
        try {
            new MockNoConcreteGenericTypeHandler<Boolean>("error") {
            }.execute(request, response);
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
        Returnable out = new MockDirectlyHandler().execute(request, response);
        print(out);
        assertEquals("OK", out.getMessage());
    }

    @Test
    public void execute_directly_1() {
        Returnable out = new MockDirectlyHandler("error").execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: error", out.getMessage());
    }

    @Test
    public void execute_directly_2() {
        Returnable out = new MockTwiceHandler().execute(request, response);
        print(out);
        assertEquals("OK", out.getMessage());
    }

    @Test
    public void execute_directly_3() {
        Returnable out = new MockTwiceHandler("error").execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: error", out.getMessage());
    }

    @Test
    public void execute_anonymous() {
        Object out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                return 1;
            }
        }.execute(request, response);
        assertEquals(Integer.class, out.getClass());
        assertEquals(1, out);
    }

    @Test
    public void execute_anonymous_1() {
        Object out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new BadRequestException("123");
            }
        }.execute(request, response);
        assertTrue(Returnable.class.isInstance(out));
        print((Returnable) out);
        assertEquals(-1, Returnable.class.cast(out).getStatus());
    }

    @Test
    public void execute_anonymous_2() {
        Object out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertTrue(Returnable.class.isInstance(out));
        print((Returnable) out);
        assertEquals(-2, Returnable.class.cast(out).getStatus());
    }

    @Test
    public void execute_anonymous_3() {
        Object out = new BaseHandler() {
            @Override
            protected Object doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new Error("123");
            }
        }.execute(request, response);
        assertTrue(Returnable.class.isInstance(out));
        print((Returnable) out);
        assertEquals(-3, Returnable.class.cast(out).getStatus());
    }

    /**
     * anonymous handler know its concrete generic class
     */
    @Test
    public void execute_anonymous_with_t() {
        String out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                return "123";
            }
        }.execute(request, response);
        assertEquals("123", out);
    }

    @Test
    public void execute_anonymous_with_t_1() {
        Boolean out2 = new BaseHandler<Boolean>() {
            @Override
            protected Boolean doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                return false;
            }
        }.execute(request, response);
        assertEquals(false, out2);
    }

    @Test
    public void execute_anonymous_with_t_2() {
        XXXResponse out = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                return new XXXResponse();
            }
        }.execute(request, response);
        print(out);
        assertEquals(0, out.getStatus());
        assertEquals("OK", out.getMessage());
    }

    @Test
    public void execute_anonymous_with_t_3() {
        String out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_anonymous_with_t_4() {
        XXXResponse out = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals(-2, out.getStatus());
        assertTrue(out.getMessage().startsWith("[Internal Server Error] Unknown exception"));
    }

    /**
     * anonymous handler with some normal interface/simple type will return its right type when exception
     */
    @Test
    public void execute_with_basic_types() {
        Map<String, Object> out = new BaseHandler<Map<String, Object>>() {
            @Override
            protected Map<String, Object> doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_1() {
        Collection<XXXResponse> out = new BaseHandler<Collection<XXXResponse>>() {
            @Override
            protected Collection<XXXResponse> doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_2() {
        byte[] out = new BaseHandler<byte[]>() {
            @Override
            protected byte[] doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_3() {
        String out = new BaseHandler<String>() {
            @Override
            protected String doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_4() {
        Boolean out = new BaseHandler<Boolean>() {
            @Override
            protected Boolean doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_5() {
        Integer out = new BaseHandler<Integer>() {
            @Override
            protected Integer doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        assertNull(out);
    }

    @Test
    public void execute_with_basic_types_6() {
        XXXResponse out = new BaseHandler<XXXResponse>() {
            @Override
            protected XXXResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }

    @Test
    public void execute_with_basic_types_7() {
        XXXAbstractResponse out = new BaseHandler<XXXAbstractResponse>() {
            @Override
            protected XXXAbstractResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }

    @Test
    public void execute_with_basic_types_8() {
        Returnable out = new BaseHandler<Returnable>() {
            @Override
            protected Returnable doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                throw new RuntimeException("123");
            }
        }.execute(request, response);
        print(out);
        assertEquals("[Internal Server Error] Unknown exception: 123", out.getMessage());
    }


    @Test
    public void execute_returnable_with_arg_constructor() {
        try {
            ArgConstructResponse out = new BaseHandler<ArgConstructResponse>() {
                @Override
                protected ArgConstructResponse doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
                    throw new RuntimeException("123");
                }
            }.execute(request, response);
            print(out);
            fail();
        } catch (RuntimeException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("Failed to initialize returnable due to"));
        }
    }

    static class NormalObject {
    }

    static final class FinalResponse implements Returnable {
        private Object data;
        private int status;
        private String message;

        @Override
        public Object getData() {
            return data;
        }

        public FinalResponse setData(Object data) {
            this.data = data;
            return this;
        }

        @Override
        public int getStatus() {
            return status;
        }

        public FinalResponse setStatus(int status) {
            this.status = status;
            return this;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public FinalResponse setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    static class MapResponse extends HashMap<String, Object> implements Returnable {

        private static final long serialVersionUID = -5334132963096243946L;
        private Object data;
        private int status;
        private String message;

        @Override
        public Object getData() {
            return data;
        }

        public MapResponse setData(Object data) {
            this.data = data;
            return this;
        }

        @Override
        public int getStatus() {
            return status;
        }

        public MapResponse setStatus(int status) {
            this.status = status;
            return this;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public MapResponse setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    static class MockTwiceHandler extends MockDirectlyHandler {
        public MockTwiceHandler(String error) {
            super(error);
        }

        public MockTwiceHandler() {
            this(null);
        }
    }

    static class MockDirectlyHandler extends BaseHandler<Returnable> {

        private final String error;

        public MockDirectlyHandler(String error) {
            this.error = error;
        }

        public MockDirectlyHandler() {
            this(null);
        }

        @Override
        protected Returnable doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
            if (this.error != null) {
                throw new RuntimeException(this.error);
            }
            return new Returnable() {
                @Override
                public Object getData() {
                    return null;
                }

                @Override
                public int getStatus() {
                    return 0;
                }

                @Override
                public String getMessage() {
                    return "OK";
                }
            };
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
        protected T doMainLogic(HttpServletRequest request, HttpServletResponse response) throws XXXException {
            if (this.error != null) {
                throw new RuntimeException(this.error);
            }
            return null;
        }
    }

    static class ArgConstructResponse extends XXXResponse {
        public ArgConstructResponse(int a) {
        }
    }

    static class CollectionResponse extends ArrayList<Object> implements Returnable {
        private Object data;
        private int status;
        private String message;

        @Override
        public Object getData() {
            return data;
        }

        public CollectionResponse setData(Object data) {
            this.data = data;
            return this;
        }

        @Override
        public int getStatus() {
            return status;
        }

        public CollectionResponse setStatus(int status) {
            this.status = status;
            return this;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public CollectionResponse setMessage(String message) {
            this.message = message;
            return this;
        }
    }
}