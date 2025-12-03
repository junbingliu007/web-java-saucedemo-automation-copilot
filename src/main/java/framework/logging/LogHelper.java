package framework.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHelper {
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}