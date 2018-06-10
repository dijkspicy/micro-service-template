package dijkspicy.queeng.server.proxy.http;


/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
public interface CipherManager {
    CipherManager DEFAULT = new CipherManager() {
        @Override
        public String encode(String ori) {
            return ori;
        }

        @Override
        public String decode(String ori) {
            return ori;
        }
    };

    /**
     * encode
     *
     * @param ori ori string
     * @return encoded string
     */
    String encode(String ori);

    /**
     * decode
     *
     * @param ori ori string
     * @return decoded string
     */
    String decode(String ori);
}
