/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import net.openhft.hashing.LongHashFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public  final class AnnoymousFunctionBlock extends CodeBlock {

        private List<String> args;
        private ClassBlock classBlock;

        public AnnoymousFunctionBlock() {
            this.args = Collections.EMPTY_LIST;
        }

        public AnnoymousFunctionBlock withArgs(String... args) {
            this.args = Arrays.asList(args);
            return this;
        }

        
        @Override
        protected Statement generateHeader() {
            return stmt().ob().append2(String.join(", ", this.args)).cb().append("->");
        }

    }