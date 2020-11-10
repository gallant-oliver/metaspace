package io.zeta.metaspace.adapter;

public class AdapterBaseException extends RuntimeException{

    public AdapterBaseException() {
    }

    public AdapterBaseException(String message) {
        super(message);
    }

    public AdapterBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdapterBaseException(Throwable cause) {
        super(cause);
    }

    public AdapterBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
