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
public class Statement {

        StringBuilder builder = new StringBuilder();
        CodeBlock block;
        protected boolean push = false;
        private final List<String> annotations = new LinkedList<>();
        
        public Statement(CodeBlock block) {
            this.block = block;
        }

        public Statement append(AccessLevel level) {
            if (level != AccessLevel.DEFAULT) {
                builder.append(level.getName()).append(" ");
            }
            return this;
        }

        public Statement withAnnotation(String annotation)
        {
            annotations.add("@"+annotation);
            return this;
        }
        
        public List<String> getAnnotations()
        {
            return annotations;
        }
        
        public Statement append(String str) {
            builder.append(str).append(" ");
            return this;
        }

        public Statement append(boolean filter,String keyword)
        {
            if(filter)
                append(keyword);
            return this;
        }
        
        public Statement append2(String str) {
            builder.append(str);
            return this;
        }

        public Statement ob() {
            builder.append("(");
            return this;
        }

        public Statement cb() {
            builder.append(") ");
            return this;
        }

        public <T> T end() {
            if (!push) {
                builder.append(";");
                this.block.addStmt(this);
                push = true;
            }
            return (T) this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

    }