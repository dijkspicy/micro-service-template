package dijkspicy.ms.server.common;

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
public abstract class If {

    static IfThen createIf() {
        return new IfThen();
    }

    static ElseIf createElseIf() {
        return new ElseIf();
    }

    protected void finish() {
        for (Pair<BooleanSupplier, DoSomething> link : this.get()) {
            if (link.getKey().getAsBoolean()) {
                link.getValue().apply();
            }
        }
    }

    protected void add(BooleanSupplier condition, DoSomething doSomething) {
        this.get().add(new Pair<>(condition, doSomething));
    }

    protected abstract List<Pair<BooleanSupplier, DoSomething>> get();

    @FunctionalInterface
    interface DoSomething {
        DoSomething NULL = () -> {
        };

        void apply();
    }

    static class ElseIf extends If {
        private final List<Pair<BooleanSupplier, DoSomething>> links = new LinkedList<>();

        private ElseIf() {
        }

        public ElseIf elseIf(BooleanSupplier condition, DoSomething doSomething) {
            this.add(condition, doSomething);
            return this;
        }

        public void elseIf(BooleanSupplier condition, DoSomething doSomething, DoSomething elseDo) {
            this.add(condition, doSomething);
            this.add(() -> true, elseDo);
            this.finish();
        }

        @Override
        protected void finish() {
            for (Pair<BooleanSupplier, DoSomething> link : this.get()) {
                if (link.getKey().getAsBoolean()) {
                    link.getValue().apply();
                    break;
                }
            }
        }

        @Override
        protected List<Pair<BooleanSupplier, DoSomething>> get() {
            return this.links;
        }
    }

    static class IfThen extends If {

        private final List<Pair<BooleanSupplier, DoSomething>> links = new LinkedList<>();

        private IfThen() {
        }

        public IfThen ifThen(BooleanSupplier condition, DoSomething doSomething) {
            this.add(condition, doSomething);
            return this;
        }

        public void ifThen(BooleanSupplier condition, DoSomething doSomething, DoSomething elseDo) {
            this.add(condition, doSomething);
            this.add(() -> true, elseDo);
            this.finish();
        }

        @Override
        protected List<Pair<BooleanSupplier, DoSomething>> get() {
            return this.links;
        }


    }
}
