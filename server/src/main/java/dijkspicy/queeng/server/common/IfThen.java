package dijkspicy.queeng.server.common;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * micro-service-template
 *
 * @author dijkspicy
 * @date 2018/6/22
 */
public interface IfThen {

    static IfThen createIf() {
        final List<Pair<BooleanSupplier, DoSomething>> links = new LinkedList<>();
        return () -> links;
    }

    static IfThen createElseIf() {
        final List<Pair<BooleanSupplier, DoSomething>> links = new LinkedList<>();
        return (IfThenFinished) () -> links;
    }

    List<Pair<BooleanSupplier, DoSomething>> get();

    default IfThen ifThen(BooleanSupplier condition, DoSomething ifSo) {
        this.get().add(new Pair<>(condition, ifSo));
        return this;
    }

    default void finish(BooleanSupplier condition, DoSomething ifSo) {
        this.get().add(new Pair<>(condition, ifSo));
        this.finish();
    }

    default void finish(BooleanSupplier condition, DoSomething ifSo, DoSomething elseIf) {
        this.get().add(new Pair<>(condition, ifSo));
        this.get().add(new Pair<>(() -> true, elseIf));
        this.finish();
    }

    default void finish() {
        for (Pair<BooleanSupplier, DoSomething> link : this.get()) {
            if (link.getKey().getAsBoolean()) {
                link.getValue().apply();
            }
        }
    }

    @FunctionalInterface
    interface DoSomething {

        void apply();
    }

    interface IfThenFinished extends IfThen {
        @Override
        default void finish() {
            for (Pair<BooleanSupplier, DoSomething> link : this.get()) {
                if (link.getKey().getAsBoolean()) {
                    link.getValue().apply();
                    break;
                }
            }
        }
    }
}
