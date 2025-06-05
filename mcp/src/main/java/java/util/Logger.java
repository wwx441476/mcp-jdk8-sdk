package java.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public interface Logger extends org.slf4j.Logger {
    default boolean on(){
        return  isDebugEnabled();
    }

    default void log(String msg){
        debug(msg);
    }

    default void log(String msg,Object t){
        debug(msg,t);
    }

    default void log(String msg,Object... t){
        debug(msg,t);
    }

    static Logger getLogger(Class<?> clazz) {
        return new Logger() {
            private final org.slf4j.Logger logger = LoggerFactory.getLogger(clazz);
            @Override
            public String getName() {
                return logger.getName();
            }

            @Override
            public boolean isTraceEnabled() {
                return logger.isTraceEnabled();
            }

            @Override
            public void trace(String s) {
                logger.trace(s);
            }

            @Override
            public void trace(String s, Object o) {
                logger.trace(s, o);
            }

            @Override
            public void trace(String s, Object o, Object o1) {
                logger.trace(s, o, o1);
            }

            @Override
            public void trace(String s, Object... objects) {
                logger.trace(s, objects);
            }

            @Override
            public void trace(String s, Throwable throwable) {
                logger.trace(s, throwable);
            }

            @Override
            public boolean isTraceEnabled(Marker marker) {
                return false;
            }

            @Override
            public void trace(Marker marker, String s) {

            }

            @Override
            public void trace(Marker marker, String s, Object o) {

            }

            @Override
            public void trace(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void trace(Marker marker, String s, Object... objects) {

            }

            @Override
            public void trace(Marker marker, String s, Throwable throwable) {

            }

            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public void debug(String s) {

            }

            @Override
            public void debug(String s, Object o) {

            }

            @Override
            public void debug(String s, Object o, Object o1) {

            }

            @Override
            public void debug(String s, Object... objects) {

            }

            @Override
            public void debug(String s, Throwable throwable) {

            }

            @Override
            public boolean isDebugEnabled(Marker marker) {
                return false;
            }

            @Override
            public void debug(Marker marker, String s) {

            }

            @Override
            public void debug(Marker marker, String s, Object o) {

            }

            @Override
            public void debug(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void debug(Marker marker, String s, Object... objects) {

            }

            @Override
            public void debug(Marker marker, String s, Throwable throwable) {

            }

            @Override
            public boolean isInfoEnabled() {
                return false;
            }

            @Override
            public void info(String s) {

            }

            @Override
            public void info(String s, Object o) {

            }

            @Override
            public void info(String s, Object o, Object o1) {

            }

            @Override
            public void info(String s, Object... objects) {

            }

            @Override
            public void info(String s, Throwable throwable) {

            }

            @Override
            public boolean isInfoEnabled(Marker marker) {
                return false;
            }

            @Override
            public void info(Marker marker, String s) {

            }

            @Override
            public void info(Marker marker, String s, Object o) {

            }

            @Override
            public void info(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void info(Marker marker, String s, Object... objects) {

            }

            @Override
            public void info(Marker marker, String s, Throwable throwable) {

            }

            @Override
            public boolean isWarnEnabled() {
                return false;
            }

            @Override
            public void warn(String s) {

            }

            @Override
            public void warn(String s, Object o) {

            }

            @Override
            public void warn(String s, Object... objects) {

            }

            @Override
            public void warn(String s, Object o, Object o1) {

            }

            @Override
            public void warn(String s, Throwable throwable) {

            }

            @Override
            public boolean isWarnEnabled(Marker marker) {
                return false;
            }

            @Override
            public void warn(Marker marker, String s) {

            }

            @Override
            public void warn(Marker marker, String s, Object o) {

            }

            @Override
            public void warn(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void warn(Marker marker, String s, Object... objects) {

            }

            @Override
            public void warn(Marker marker, String s, Throwable throwable) {

            }

            @Override
            public boolean isErrorEnabled() {
                return false;
            }

            @Override
            public void error(String s) {

            }

            @Override
            public void error(String s, Object o) {

            }

            @Override
            public void error(String s, Object o, Object o1) {

            }

            @Override
            public void error(String s, Object... objects) {

            }

            @Override
            public void error(String s, Throwable throwable) {

            }

            @Override
            public boolean isErrorEnabled(Marker marker) {
                return false;
            }

            @Override
            public void error(Marker marker, String s) {

            }

            @Override
            public void error(Marker marker, String s, Object o) {

            }

            @Override
            public void error(Marker marker, String s, Object o, Object o1) {

            }

            @Override
            public void error(Marker marker, String s, Object... objects) {

            }

            @Override
            public void error(Marker marker, String s, Throwable throwable) {

            }
        };
    }
}
