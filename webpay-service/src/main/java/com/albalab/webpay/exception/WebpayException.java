package com.albalab.webpay.exception;

public class WebpayException extends RuntimeException {

    public WebpayException(String mensaje) {
        super(mensaje);
    }

    public WebpayException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
