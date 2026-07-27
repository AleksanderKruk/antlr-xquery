package com.github.akruk.visitorannotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Visitor {

    /**
     * Classes to be visited.
     */
    Class<?>[] classes();

    /**
     * Name of the visitor
     */
    String name();
    
}