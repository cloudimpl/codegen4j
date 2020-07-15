/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j;

import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author nuwansa
 */
public class JavaFile {
    
    private final ClassBlock block;

    private JavaFile(ClassBlock block) {
        this.block = block;
    }
    
    
    public static JavaFile wrap(ClassBlock block)
    {
        return new JavaFile(block);
    }
    
    public String writeTo(File folder)
    {
        if(!folder.isDirectory())
            throw new RuntimeException(folder +" is not a directory");
        
        String path = folder.getAbsolutePath();
        if(block.getPackageName() != null)
            path = path+"/"+block.getPackageName().replaceAll("\\.", "/");
        
        File dir = new File(path);
        dir.mkdirs();
        String javaFile = dir.getAbsoluteFile()+"/"+block.getClassName()+".java";
        try(FileWriter writer = new FileWriter(javaFile))
        {
            writer.write(generateCode());
        }catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        return javaFile;
    }
    
    public String generateCode()
    {
        StringBuilder builder = new StringBuilder();
        int tabIndex = 0;
        if (block.getPackageName() != null) {
            builder.append("package ").append(block.getPackageName()).append(";").append("\r\n").append("\r\n");
        }
        block.getImports().forEach(imp -> builder.append("import ").append(imp).append(";").append("\r\n"));
        builder.append("\r\n");
        block.generateCode(tabIndex, builder);
        return builder.toString();
    }
}
