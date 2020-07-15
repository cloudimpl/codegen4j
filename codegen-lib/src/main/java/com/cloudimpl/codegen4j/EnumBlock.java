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
public class EnumBlock extends ClassBlock{

    public EnumBlock(String enumName) {
        super(enumName);
    }
    
    public EnumStatement enumStmt(String val)
    {
        return new EnumStatement(val, this);
    }
    
    @Override
    protected Statement generateHeader() {
        return stmt().append(level).append("enum").append(className)
                .append(!implementList.isEmpty(),"implements "+String.join(",", implementList));
    }
    
}
