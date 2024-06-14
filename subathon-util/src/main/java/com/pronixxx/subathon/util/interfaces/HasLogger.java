package com.pronixxx.subathon.util.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogger {
    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
