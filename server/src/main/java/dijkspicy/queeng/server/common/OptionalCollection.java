package dijkspicy.queeng.server.common;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * dijkspicy-commons
 *
 * @author dijkspicy
 * @date 2018/5/25
 */
public final class OptionalCollection<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final OptionalCollection EMPTY = new OptionalCollection();

    /**
     * If true then the value is present, otherwise indicates no value is present
     */
    private final boolean isPresent;
    private final Collection<T> value;

    /**
     * Construct an empty instance.
     *
     * @implNote generally only one empty instance, {@link java.util.OptionalDouble#EMPTY},
     * should exist per VM.
     */
    private OptionalCollection() {
        this.isPresent = false;
        this.value = Collections.emptyList();
    }

    /**
     * Construct an instance with the value present.
     *
     * @param value the double value to be present.
     */
    private OptionalCollection(Collection<T> value) {
        this.isPresent = !this.isEmpty(value);
        this.value = value;
    }

    /**
     * Returns an empty {@code OptionalDouble} instance.  No value is present for this
     * OptionalDouble.
     *
     * @return an empty {@code OptionalDouble}.
     * @apiNote Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Option.empty()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     */
    public static OptionalCollection empty() {
        return EMPTY;
    }

    /**
     * Return an {@code OptionalDouble} with the specified value present.
     *
     * @param value the value to be present
     * @return an {@code OptionalDouble} with the value present
     */
    public static <T> OptionalCollection<T> of(Collection<T> value) {
        return new OptionalCollection<>(value);
    }

    /**
     * Have the specified consumer accept the value if a value is present,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is
     *                              null
     */
    public void ifPresent(Consumer<Collection<T>> consumer) {
        if (consumer == null) {
            return;
        }
        if (isPresent) {
            consumer.accept(value);
        }
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present
     * @return the value, if present, otherwise {@code other}
     */
    public Collection<T> orElse(Collection<T> other) {
        return isPresent ? value : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code DoubleSupplier} whose result is returned if no value
     *              is present
     * @return the value if present otherwise the result of {@code other.getAsDouble()}
     * @throws NullPointerException if value is not present and {@code other} is
     *                              null
     */
    public Collection<T> orElseGet(Supplier<Collection<T>> other) {
        Objects.requireNonNull(other, "NPE of other supplier");
        return isPresent ? value : other.get();
    }

    /**
     * Return the contained value, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     *                          be thrown
     * @return the present value
     * @throws X                    if there is no value present
     * @throws NullPointerException if no value is present and
     *                              {@code exceptionSupplier} is null
     * @apiNote A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     */
    public <X extends Throwable> Collection<T> orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPresent, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OptionalCollection that = (OptionalCollection) o;
        return isPresent == that.isPresent && Objects.equals(value, that.value);
    }

    /**
     * If a value is present in this {@code OptionalDouble}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the value held by this {@code OptionalDouble}
     * @throws NoSuchElementException if there is no value present
     * @see java.util.OptionalDouble#isPresent()
     */
    public Collection<T> get() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return isPresent;
    }

    private boolean isEmpty(Collection<T> value) {
        if (value == null) {
            return true;
        }
        return value.isEmpty();
    }
}
