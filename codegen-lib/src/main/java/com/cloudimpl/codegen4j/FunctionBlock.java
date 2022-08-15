/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public  final class FunctionBlock extends PermissionBlock {

        protected String functionName;
        protected String returnType = "void";
        private List<String> args;
        private ClassBlock classBlock;

        public FunctionBlock(String functionName,ClassBlock classBlock) {
            this.functionName = functionName;
            this.returnType = returnType;
            this.args = Collections.EMPTY_LIST;
            this.classBlock = classBlock;
        }

        public ClassBlock getClassBlock()
        {
            return this.classBlock;
        }

        public FunctionBlock withArgs(String... args) {
            this.args = Arrays.asList(args);
            return this;
        }

        public FunctionBlock withReturnType(String returnType) {
            this.returnType = returnType;
            return this;
        }
        
        @Override
        protected Statement generateHeader() {
            return stmt().append(level.getName()).append(isStatic, "static")
                    .append(isFinal,"final")
                    .append(returnType)
                    .append2(functionName).ob().append2(String.join(", ", this.args)).cb();
        }
    }