package dijkspicy.ms.server.dispatch;

import dijkspicy.ms.base.errors.BadRequestException;
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


    }
}