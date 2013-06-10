package com.projectsexception.myapplist.xml;


public class ParserException extends Throwable {

    private static final long serialVersionUID = -6741657467127834584L;
    
    public ParserException(String mensaje) {
        super(mensaje);
    }

    public ParserException(Exception e) {
        super(e);
    }

}
