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
public class Var extends Statement {

        protected AccessLevel level = AccessLevel.DEFAULT;
        protected String type;
        protected String var;
        protected Object val;
        protected boolean isFinal = false;
        protected boolean isStatc = false;
        
        public Var(CodeBlock block, String type, String var) {
            super(block);
            this.var = var;
            this.type = type;
        }

        public Var withFinal() {
            this.isFinal = true;
            return this;
        }

        public Var withStatic() {
            this.isStatc = true;
            return this;
        }
        
        public Var withNull() {
            this.val = "null";
            return this;
        }

        public Var withAccess(AccessLevel level) {
            this.level = level;
            return this;
        }

        public Var assign(String val) {
            this.val = val;
            return this;
        }

        public Var assingObj(JavaObject obj){
            this.val = obj.toString();
            return this;
        }

        public Var assignFunction(AnnoymousFunctionBlock functionBlock)
        {
            this.val = functionBlock;
            return this;
        }

        public Object getVal()
        {
            return this.val;
        }
        public <T> T end() {
            if (!push) {
                append(level);
                append(isStatc,"static");
                append(isFinal, "final");

                append(type);
                append(var);
                this.block.addStmt(this);
                push = true;
            }
            return (T) this;
        }
    }