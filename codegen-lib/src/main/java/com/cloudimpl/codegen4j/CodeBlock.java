/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import static com.cloudimpl.codegen4j.ClassBuilder.tab;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nuwansa
 */
public abstract class CodeBlock {

    private List<Statement> stmts = new LinkedList<>();
    protected List<CodeBlock> codeBlocks = new LinkedList<>();
    private List<String> annotations = new LinkedList<>();
    private final Set<String> imports = new HashSet<>();
    private boolean disableBlockSpace = false;

    public CodeBlock() {
    }

    public static String createName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    protected void addStmt(Statement stmt) {
        stmts.add(stmt);
    }

    public <T extends CodeBlock> T withImports(String... imports) {
        Arrays.asList(imports).stream().filter(p -> !p.substring(0, p.lastIndexOf(".")).equals("java.lang")).forEach(imp -> this.imports.add(imp));
        return (T)this;
    }

    protected Collection<String> collectImports(Set<String> imports)
    {
       imports.addAll(this.imports);
       codeBlocks.forEach(codeBlock -> codeBlock.collectImports(imports));
       return imports;
    }

    private boolean hasEnumConstant() {
        return this.stmts.stream().filter(st -> st instanceof EnumStatement).findAny().orElse(null) != null;
    }

    protected final void disableBlockSpace() {
        this.disableBlockSpace = true;
    }

    public <T extends CodeBlock> T withAnnotation(String annotation) {
        this.annotations.add("@" + annotation);
        return (T) this;
    }

    public Statement stmt() {
        return new Statement(this);
    }

    public ReturnStatement withReturnStatment(String returnVal) {
        return new ReturnStatement(this, returnVal);
    }

    public SwitchBlock createSwitch(String switchName) {
        return pushBlock(new SwitchBlock(switchName));
    }

    public RawCodeBlock createBlock(String header){
        return pushBlock(new RawCodeBlock(header));
    }
    
    public ConditionalBlock createIf(String args) {
        return pushBlock(new ConditionalBlock("if", args));
    }

    public ConditionalBlock createElseIf(String args) {
        return pushBlock(new ConditionalBlock("else if", args));
    }

    public ConditionalBlock createElse(String args) {
        return pushBlock(new ConditionalBlock("else", args));
    }

    public SynchronousBlock createSynchronousBlock(String args) {
        return pushBlock(new SynchronousBlock(args));
    }

    protected <T> T pushBlock(CodeBlock block) {
        this.codeBlocks.add(block);
        return (T) block;
    }

    public Var var(String type, String var) {
        return new Var(this, type, var);
    }

    protected abstract Statement generateHeader();

    protected void generateCode(int tabIndex, StringBuilder builder) {
        int beginTab = tabIndex;
        Statement header = generateHeader();
        if (header != null) {
            this.annotations.forEach(s -> {
                tab(builder, beginTab);
                builder.append(s).append("\r\n");
            });
            tab(builder, beginTab);
            builder.append(header.toString());
        } else {
            tab(builder, beginTab);
        }
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
        codeBlocks.stream().filter(sb -> sb instanceof StaticBlock).forEach(sb -> sb.generateCode(temp, builder));
        ReturnStatement returnStmt = (ReturnStatement) stmts.stream().filter(s -> s instanceof ReturnStatement).findFirst().orElse(null);
        stmts.removeIf(stmt -> stmt instanceof ReturnStatement);
        codeBlocks.removeIf(cb -> cb instanceof StaticBlock);
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
            if (arg0.getClass() == arg1.getClass()) {
                return 0;
            }
            if (arg0.getClass() == ClassBlock.class || arg0.getClass() == EnumBlock.class) {
                return 1;
            } else {
                return 0;
            }
        });
        int temp2 = tabIndex;
        codeBlocks.forEach(cb -> {
            if (!disableBlockSpace && !(cb instanceof ConditionalBlock)) {
                builder.append("\r\n");
            }
            cb.generateCode(temp2, builder);
            //     builder.append("\r\n");
        });
        tabIndex--;

        if (returnStmt != null) {
            tab(builder, temp2);
            builder.append(returnStmt.toString()).append("\r\n");
        }
        tab(builder, beginTab);
        builder.append("}").append("\r\n");
    }
}
