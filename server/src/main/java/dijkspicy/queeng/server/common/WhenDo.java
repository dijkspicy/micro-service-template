package dijkspicy.queeng.server.common;

import java.util.function.BooleanSupplier;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/22
 */
public class WhenDo {

    private final boolean elseIf;
    private boolean finished;

    private WhenDo(boolean elseIf) {
        this.elseIf = elseIf;
    }

    public static WhenDo createIf() {
        return new WhenDo(false);
    }

    public static WhenDo createElseIf() {
        return new WhenDo(true);
    }

    public WhenDo whenDo(BooleanSupplier condition, DoSomething ifSo) {
        this.whenDo(condition, ifSo, DoSomething.NULL);
        return this;
    }

    public void whenDo(BooleanSupplier condition, DoSomething ifSo, DoSomething elseIf) {
        if (this.elseIf && this.finished) {
            return;
        }

        if (condition.getAsBoolean()) {
            ifSo.apply();
            this.finished = true;
        } else {
            elseIf.apply();
        }
    }

    @FunctionalInterface
    interface DoSomething {
        DoSomething NULL = () -> {
        };

        void apply();
    }
}
