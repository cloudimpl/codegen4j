/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

/**
 *
 * @author nuwansa
 */
public enum AccessLevel {
    DEFAULT(""),
    PRIVATE("private"),
    PUBLIC("public"),
    PROTECTED("protected");
    
    private final String name;

    private AccessLevel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
