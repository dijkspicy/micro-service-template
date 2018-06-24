package dijkspicy.ms.server.common;

import dijkspicy.ms.server.persistence.DAO;
import dijkspicy.ms.server.persistence.XXDAO;
import dijkspicy.ms.server.proxy.XXProxy;
import org.apache.naming.factory.BeanFactory;
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
                .elseIf(() -> isBool(a, 1), () -> System.out.println(1))
                .elseIf(() -> isBool(a, 3), () -> System.out.println("ok"))
                .elseIf(() -> isBool(a, 7), () -> System.out.println(7))
                .elseIf(() -> isBool(a, 9), () -> System.out.println(9), () -> System.out.println("nothing"));
    }

    private static boolean isBool(int a, int i) {
        System.out.println(a == i);
        return a == i;
    }

    @Test
    public void name() {
        System.out.println(DAO.getInstance(XXDAO.class));
    }
}