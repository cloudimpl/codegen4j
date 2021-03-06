/*
 * Copyright 2020 nuwansa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.codegen4j;

/**
 *
 * @author nuwansa
 */
public class SwitchBlock extends CodeBlock{
    private final String switchArg;

    public SwitchBlock(String switchArg) {
        this.switchArg = switchArg;
        disableBlockSpace();
    }

    public CaseBlock createCase(String caseName)
    {
        return pushBlock(new CaseBlock(caseName));
    }
    
    public DefaultBlock createDefault()
    {
        return pushBlock(new DefaultBlock());
    }
    
    @Override
    protected Statement generateHeader() {
        return stmt().append("switch").ob().append(switchArg).cb();
    }
}
