package dijkspicy.ms.server.dispatch;

import dijkspicy.ms.server.common.errors.BadRequestException;
import org.junit.Test;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/7/10
 */
public class ReturnableTest {

    @Test
    public void serialize() {
        System.out.println(new BadRequestException("adsf", new IllegalArgumentException("adf")));
    }

    static class TestLoop extends ServiceResponse {

        @Override
        public int getRetCode() {
            return 0;
        }

        @Override
        public String getRetInfo() {
            return "test";
        }

        public TestLoop getA() {
            return this;
        }

    }
}