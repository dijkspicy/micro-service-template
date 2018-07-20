package dijkspicy.ms.base;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

/**
 * XXXAbstractResponse
 *
 * @author dijkspicy
 * @date 2018/7/17
 */
public abstract class XXXAbstractResponse implements Returnable {
    public static void main(String[] args) throws IOException {
        Object out = RET_MAPPER.readValue("{}", new TypeReference<XXXAbstractResponse>() {
        });
        System.out.println(out);
    }
}
