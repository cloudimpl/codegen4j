/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author nuwansa
 */
public class JavaObject{
    private final String cls;
    private List<String> args;

    public static JavaObject create(String clsName)
    {
        return new JavaObject(clsName);
    }
    
    private JavaObject(String cls) {
        this.cls = cls;
        this.args = Collections.EMPTY_LIST;
    }
    
    public static List<String> parseArgs(Object... args)
    {
        return Arrays.asList(args).stream().map(arg->{
            if(arg instanceof String)
                return "\""+arg+"\"";
            else
                return arg;
        }).map(arg->""+arg).collect(Collectors.toList());
    }
    
    public JavaObject withArgs(Object... args)
    {
        this.args = parseArgs(args);
        return this;
    }
    
    @Override
    public String toString()
    {
        return MessageFormat.format("new {0}({1})", this.cls,String.join(", ",this.args));
    }
}
