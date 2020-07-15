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
public class ClassBuilder {

//    private String packageName;
//    private Set<String> imports = new HashSet<>();
    private List<CodeBlock> codeBlocks = new LinkedList<>();

    public ClassBuilder() {
    }
//
//    public ClassBuilder withPackageName(String packageName) {
//        this.packageName = packageName;
//        return this;
//    }
//
//    public ClassBuilder withImports(String... imports) {
//        Arrays.asList(imports).forEach(imp -> this.imports.add(imp));
//        return this;
//    }

    public List<CodeBlock> getCodeBlocks()
    {
        return codeBlocks;
    }
    
    public ClassBlock createClass(String name) {
        ClassBlock block = new ClassBlock(name);
        codeBlocks.add(block);
        return block;
    }

    public EnumBlock createEnum(String name){
        EnumBlock enumBlock = new EnumBlock(name);
        codeBlocks.add(enumBlock);
        return enumBlock;
    }
    
//    public String generateCode() {
//        StringBuilder builder = new StringBuilder();
//        int tabIndex = 0;
//        if (packageName != null) {
//            builder.append("package ").append(packageName).append(";").append("\r\n").append("\r\n");
//        }
//        imports.forEach(imp -> builder.append("import ").append(imp).append(";").append("\r\n"));
//        builder.append("\r\n");
//        codeBlocks.forEach(cb -> cb.generateCode(tabIndex, builder));
//        return builder.toString();
//    }

    public static void tab(StringBuilder builder, int count) {
        while (count > 0) {
            builder.append("    ");
            count--;
        }
    }

    public static void main(String[] args) {
        ClassBuilder builder = new ClassBuilder();
        ClassBlock classBlock = builder.createClass("Test").withStatic().withFinal().withAccess(AccessLevel.PUBLIC);
        Var var = classBlock.var("String", "school").withAccess(AccessLevel.PROTECTED)
                .withFinal().withNull().assign("Richmond").end();
        classBlock.stmt().append(AccessLevel.PRIVATE).append("int").append("age").end();
        classBlock.stmt().append(AccessLevel.PUBLIC).append("String").append("name").end();
        classBlock.createConstructor().withAccess(AccessLevel.PUBLIC);
        classBlock.createConstructor("String name", "int age");
        
        FunctionBlock func = classBlock.createFunction("set").withArgs("int age", "String name").withAccess(AccessLevel.PUBLIC);
        func.stmt().append("this.age = age").end();
        func.stmt().append("this.name = name").end();
        classBlock.createGetter(var);
        classBlock.createSetter(var);
       // System.out.println(builder.generateCode());
    }
}
