package dijkspicy.ms.server.dispatch;

/**
 * ServiceResponse
 *
 * @author dijkspicy
 * @date 2018/6/18
 */
public class ServiceResponse implements Returnable {
    private int retCode = OK.retCode;
    private String retInfo = OK.retInfo;

    @Override
    public int getRetCode() {
        return this.retCode;
    }

    public ServiceResponse setRetCode(int retCode) {
        this.retCode = retCode;
        return this;
    }

    @Override
    public String getRetInfo() {
        return this.retInfo;
    }

    public ServiceResponse setRetInfo(String retInfo) {
        this.retInfo = retInfo;
        return this;
    }

    @Override
    public String toString() {
        return this.serialize();
    }
}
