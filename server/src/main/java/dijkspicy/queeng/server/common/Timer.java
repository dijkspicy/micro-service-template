package dijkspicy.queeng.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public class Timer implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);
    private final Object target;
    private final LocalDateTime start;

    public Timer(Object target) {
        this.target = target;
        this.start = LocalDateTime.now();
        LOGGER.info("It starts: {}", this.target);
    }

    public Duration duration() {
        return Duration.between(this.start, LocalDateTime.now());
    }

    @Override
    public void close() {
        long millis = this.duration().toMillis();
        LOGGER.info("It takes {} millis: {}", millis, this.target);
    }
}
