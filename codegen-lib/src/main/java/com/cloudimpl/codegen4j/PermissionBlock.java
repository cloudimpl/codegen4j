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
public abstract class PermissionBlock extends CodeBlock {

    protected AccessLevel level = AccessLevel.DEFAULT;
    protected boolean isStatic = false;
    protected boolean isFinal = false;
    
    public PermissionBlock() {
    }

    public <T extends PermissionBlock> T withAccess(AccessLevel level) {
        this.level = level;
        return (T) this;
    }

    public <T extends PermissionBlock> T withStatic()
    {
        this.isStatic = true;
        return (T) this;
    }
    
    public <T extends PermissionBlock> T withFinal()
    {
        this.isFinal = true;
        return (T) this;
    }
}
