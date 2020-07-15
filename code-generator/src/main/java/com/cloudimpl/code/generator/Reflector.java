/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.code.generator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 *
 * @author nuwansa
 */
public class Reflector {

    private Class<?> provider;
    private Object instance;
    private Method addProperty;
    private Method setProject;
    private Method getCodeGenFolder;
    private Method execute;
    private Method addDependacyPath;
    
    public Reflector(Class<?> provider) {
        this.provider = provider;
        try {
            addProperty = findMethod(provider, "addProperty", false);
            setProject = findMethod(provider, "setProject", false);
            getCodeGenFolder = findMethod(provider, "getCodeGenFolder", false);
            execute = findMethod(provider, "execute", false);
            addDependacyPath = findMethod(provider,"addDependacyPath", false);
            
            this.instance = this.provider.getConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }

    public void addProperty(String key, String value) {
        try {
            addProperty.invoke(instance, key, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }

     public void addDependacyPath(String path,boolean runtime) {
        try {
            addDependacyPath.invoke(instance, path,runtime);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }
     
    public void execute() {
        try {
            execute.invoke(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }

    public File getCodeGenFolder() {
        try {
            return new File((String) getCodeGenFolder.invoke(instance));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }

    public void setProject(String baseDir, String buildDir, String buildTargetDir) {
        try {
            setProject.invoke(instance, baseDir, buildDir, buildTargetDir);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ReflectorException(ex);
        }
    }

    private Method findMethod(Class cls, String methodName, boolean isStatic) {

        do {
            Method method = Arrays.asList(cls.getDeclaredMethods()).stream().filter(m -> m.getName().equals(methodName))
                    .filter(m -> Modifier.isStatic(m.getModifiers()) == isStatic)
                    .findFirst().orElse(null);
            if (method != null) {
                method.setAccessible(true);
                  return method;
            }
            cls = cls.getSuperclass();
        } while (cls != Object.class);
        throw new ReflectorException("method : " + methodName + " not found in " + cls.getName());
    }

    public static final class ReflectorException extends RuntimeException {

        public ReflectorException(Throwable thr) {
            super(thr);
        }

        public ReflectorException(String msg) {
            super(msg);
        }
    }
}
