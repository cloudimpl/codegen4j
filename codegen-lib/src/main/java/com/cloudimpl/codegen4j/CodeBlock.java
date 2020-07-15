/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import static com.cloudimpl.codegen4j.ClassBuilder.tab;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public abstract class CodeBlock {

    private List<Statement> stmts = new LinkedList<>();
    private List<CodeBlock> codeBlocks = new LinkedList<>();
    private List<String> annotations = new LinkedList<>();

    public CodeBlock() {
    }

    public static String createName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    protected void addStmt(Statement stmt) {
        stmts.add(stmt);
    }

    private boolean hasEnumConstant()
    {
        return this.stmts.stream().filter(st->st instanceof EnumStatement).findAny().orElse(null) != null;
    }
    
    public <T extends CodeBlock> T withAnnotation(String annotation) {
        this.annotations.add("@" + annotation);
        return (T) this;
    }

    public Statement stmt() {
        return new Statement(this);
    }

    protected <T> T pushBlock(CodeBlock block) {
        this.codeBlocks.add(block);
        return (T) block;
    }

    public Var var(String type, String var) {
        return new Var(this, type, var);
    }

    protected abstract Statement generateHeader();

    public void generateCode(int tabIndex, StringBuilder builder) {
        int beginTab = tabIndex;
        Statement header = generateHeader();
        if (header != null) {
            this.annotations.forEach(s -> {
                tab(builder, beginTab);
                builder.append(s).append("\r\n");
            });
            tab(builder, beginTab);
            builder.append(header.toString());
        }
        else
            tab(builder, beginTab);
        builder.append("{").append("\r\n");

        tabIndex++;
        int temp = tabIndex;
        //builder.append("\r\n");
        if (this instanceof EnumBlock) {
            if (!hasEnumConstant()) {
                tab(builder, temp);
                builder.append(";").append("\r\n");
            }
        }
        stmts.forEach(stmt -> {
            stmt.getAnnotations().forEach(s -> {
                tab(builder, temp);
                builder.append(s).append("\r\n");
            });
            tab(builder, temp);
            builder.append(stmt.toString()).append("\r\n");
        });
        //builder.append("\r\n");
        codeBlocks.sort((CodeBlock arg0, CodeBlock arg1) -> {
            if(arg0.getClass() == arg1.getClass())
                return 0;
            if(arg0.getClass() == ClassBlock.class || arg0.getClass() == EnumBlock.class)
                return 1;
            else
                return -1;
        });
        int temp2 = tabIndex;
        codeBlocks.forEach(cb -> {
            builder.append("\r\n");
            cb.generateCode(temp2, builder);
       //     builder.append("\r\n");
        });
        tabIndex--;
        tab(builder, beginTab);
        builder.append("}").append("\r\n");
    }
}
