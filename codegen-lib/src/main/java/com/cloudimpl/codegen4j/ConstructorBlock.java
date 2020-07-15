/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public  final class ConstructorBlock extends PermissionBlock {

    protected List<String> args;
    protected String className;

    public ConstructorBlock(String className, String... args) {
        this.className = className;
        this.args = Arrays.asList(args);
    }

    protected Statement generateHeader() {
        return stmt().append(level).append2(className).ob()
                .append2(String.join(", ", this.args)).cb();
    }
}
