package com.ft.aem.poet;

/**
 * exception class for po.et
 */
public class PoetException extends Exception {
    public PoetException(String s) {
        super(s);
    }

    public PoetException(String s, Exception e) {
        super(s, e);
    }
}
