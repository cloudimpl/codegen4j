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
public class Clazz {
    private final String cls;

    public Clazz(String cls) {
        this.cls = cls;
    }

    public String getCls() {
        return cls;
    }

    @Override
    public String toString() {
        return cls;
    }
    
    
    public static Clazz wrap(String val)
    {
        return new Clazz(val);
    }
    
}
