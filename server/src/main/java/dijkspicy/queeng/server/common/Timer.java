package dijkspicy.queeng.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ODAEQueryEngineService
 *
 * @author dijkspicy
 * @date 2018/6/8
 */
public interface Timer {
    Logger LOGGER = LoggerFactory.getLogger(Timer.class);

    /**
     * start with target
     *
     * @param target target
     * @return timer
     */
    static AutoCloseable start(Object target) {
        LocalDateTime start = LocalDateTime.now();
        LOGGER.info("It starts: {}", target);
        return () -> {
            long millis = Duration.between(start, LocalDateTime.now()).toMillis();
            LOGGER.info("It takes {} ms: {}", millis, target);
        };
    }
}