/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.openhft.hashing.LongHashFunction;

/**
 *
 * @author nuwansa
 */
public class ClassBlock extends PermissionBlock {

    private String packageName;

    protected String className = null;
    private String extend = null;
    protected List<String> implementList;
    private boolean enableSerializedId;

    public ClassBlock(String name) {
        this.className = name;
        this.implementList = new LinkedList<>();
        this.enableSerializedId = false;
    }

    public String getClassName() {
        return this.className;
    }

    public ClassBlock enableSerializedId(boolean enable) {
        this.enableSerializedId = enable;
        return this;
    }

    public ClassBlock extend(String className) {
        this.extend = className;
        return this;
    }

    public <T extends ClassBlock> T withPackageName(String packageName) {
        this.packageName = packageName;
        return (T)this;
    }

    public String getPackageName() {
        return packageName;
    }

    public Collection<String> getImports() {
        Set<String> imports = new HashSet<>(this.getImports());
        imports.addAll(codeBlocks.stream().flatMap(codeBlock -> codeBlock.getImports().stream()).collect(Collectors.toSet()));
        return imports;
    }

    public ClassBlock implement(String... clsList) {
        implementList.addAll(Arrays.asList(clsList));
        return this;
    }

    public ClassBlock createClass(String name) {
        return pushBlock(new ClassBlock(name));
    }

    public StaticBlock createStaticBlock(){
        return pushBlock(new StaticBlock());
    }
    
    public ConstructorBlock createConstructor(String... args) {
        return pushBlock(new ConstructorBlock(className, args));
    }

    public FunctionBlock createFunction(String functionName) {
        return pushBlock(new FunctionBlock(functionName));
    }

    public void emptyBlock() {
        pushBlock(new CodeBlock() {
            @Override
            protected Statement generateHeader() {
                return null;
            }
        });
    }

    public FunctionBlock createGetter(Var var) {
        FunctionBlock func = new FunctionBlock("get" + (("" + var.var.charAt(0)).toUpperCase() + var.var.substring(1)));
        func.stmt().append("return").append2("this.").append(var.var).end();
        pushBlock(func.withReturnType(var.type).withAccess(AccessLevel.PUBLIC));
        return func;
    }

    public void createSetter(Var var) {
        FunctionBlock func = new FunctionBlock("set" + (("" + var.var.charAt(0)).toUpperCase() + var.var.substring(1)))
                .withArgs(var.type + " " + var.var);
        func.stmt().append2("this.").append(var.var).append("=").append(var.var).end();
        pushBlock(func.withAccess(AccessLevel.PUBLIC));
    }

    @Override
    protected Statement generateHeader() {
        return stmt().append(level)
                .append(isStatic, "static")
                .append(isFinal, "final")
                .append("class").append(className)
                .append(extend != null, "extends " + extend)
                .append(!implementList.isEmpty(), "implements " + String.join(",", implementList));
    }

    @Override
    public void generateCode(int tabIndex, StringBuilder builder) {
        if (enableSerializedId) {
            StringBuilder output = new StringBuilder();
            super.generateCode(0, output);
            var("long", "serialVersionUID").withAccess(AccessLevel.PUBLIC).withFinal().withStatic().assign("" + LongHashFunction.xx().hashChars(output.toString())+"L").end();
        }
        super.generateCode(tabIndex, builder);
    }
}
