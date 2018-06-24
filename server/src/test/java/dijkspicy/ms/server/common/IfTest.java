package dijkspicy.ms.server.common;

import org.junit.Test;

/**
 * IfTest
 *
 * @author dijkspicy
 * @date 2018/6/22
 */
public class IfTest {

    @Test
    public void match() {
        int a = 3;
        If.createElseIf()
                .ifThen(() -> isBool(a, 1), () -> System.out.println(1))
                .ifThen(() -> isBool(a, 3), () -> System.out.println("ok"))
                .ifThen(() -> isBool(a, 7), () -> System.out.println(7))
                .finish(() -> isBool(a, 9), () -> System.out.println(9), () -> System.out.println("nothing"));
    }

    private static boolean isBool(int a, int i) {
        System.out.println(a == i);
        return a == i;
    }
}