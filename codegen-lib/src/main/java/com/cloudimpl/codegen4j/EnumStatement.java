/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public class EnumStatement extends Statement {

    private final List<String> args = new LinkedList<>();
    private final String constant;

    public EnumStatement(String constant, CodeBlock block) {
        super(block);
        this.constant = constant;
    }

    public EnumStatement withArgs(Object... args) {
        JavaObject.parseArgs(args).forEach(a -> this.args.add(a));
        return this;
    }

    public <T> T done() {
        if (!push) {
            append2(constant).ob().append2(String.join(", ", args)).cb();
            builder.append(",");
            this.block.addStmt(this);
            push = true;
        }
        return (T) this;
    }
    
    @Override
     public <T> T end() {
            if (!push) {
                append2(constant).ob().append2(String.join(", ", args)).cb();
                builder.append(";\r\n");
                this.block.addStmt(this);
                push = true;
            }
            return (T) this;
        }
}
