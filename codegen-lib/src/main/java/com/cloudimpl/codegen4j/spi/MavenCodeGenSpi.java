/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.codegen4j.spi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author nuwansa
 */
public abstract class MavenCodeGenSpi {

    private final Map<String, String> properties = new HashMap<>();
    private final String namespace;
    private Project project;
    private final String projectName;
    private final String propertyPrefix;
    private final Set<String> artifacts = new HashSet<>();
    private Optional<String> classPath = Optional.empty();
    private List<String> runtimeClasspathElements = new LinkedList<>();
    private ClassLoader classLoader;
    private boolean classLoaderInit = false;
    public MavenCodeGenSpi(String projectName, String namespace) {
        this.namespace = namespace;
        this.projectName = projectName;
        this.propertyPrefix = this.namespace + "." + this.projectName + ".";
    }

    protected void addProperty(String key, String value) {
        if (key.startsWith(propertyPrefix)) {
            this.properties.put(key.substring(propertyPrefix.length()), value);
        }
    }

    protected void addDependacyPath(String path,boolean runtime)
    {
        if(!runtime)
            artifacts.add(path);
        else
            runtimeClasspathElements.add(path);
    }
    
    protected void addClassPath(String artifact)
    {
        String regex = artifact.replaceAll("\\.", "/");
        classPath = this.artifacts.stream().filter(s->s.contains(regex)).findFirst();
    }
    
    protected Class loadClass(String className)
    {

        try {
            if(classLoader == null)
                 classLoader = getClassLoader();
            Class cls =  classLoader.loadClass(className);
            return cls;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MavenCodeGenSpi.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
      
                   
    }
    
    private ClassLoader getClassLoader() {

        URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
        for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            try {
                String element = (String) runtimeClasspathElements.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(MavenCodeGenSpi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
                Thread.currentThread().getContextClassLoader());
        return newLoader;

    }
    
    public String getCodeGenFolder() {
        return project.getBuildDir() + "/generated-sources/" + projectName;
    }

    public void setProject(String baseDir, String buildDir, String buildTargetDir) {
        this.project = new Project(baseDir, buildDir, buildTargetDir);
    }

    public Project getProject() {
        return project;
    }

    public String getProperty(String prop, String defaultValue) {
        String val = properties.get(prop);
        if (val == null && defaultValue == null) {
            throw new CodeGenException("property :" + prop + " not found");
        } else if (val == null) {
            val = defaultValue;
        }
        return val;

    }

    public boolean hasProperty(String prop)
    {
        return properties.containsKey(prop);
    }
    
    protected void compileFiles(File... files)
    {
        compileFiles(Arrays.asList(files));
    }
    
    protected List<String> getAllSourceFiles()
    {
        try (Stream<Path> walk = Files.walk(Paths.get(getProject().getSourceDir()))) {
            // We want to find only regular files
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).filter(s->s.endsWith(".java")).collect(Collectors.toList());

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected void compileFiles(List<File> javaFiles) {
        if (javaFiles.isEmpty()) {
            return;
        }
        if(classPath.isEmpty())
            classPath = Optional.of(runtimeClasspathElements.stream().collect(Collectors.joining(":")));
        // File[] javaFiles = new File[]{new File(javaFile.getAbsolutePath())};
        //log("compiling files: " + javaFiles.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits
                = fileManager.getJavaFileObjectsFromFiles(javaFiles);

        List<String> optionList = new ArrayList<>();
        if (classPath.isPresent()) {
            optionList.addAll(Arrays.asList("-classpath", classPath.get()));
        }
      //  optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")));

        File targetDir = new File(project.getBuildTargetDir());
        targetDir.mkdir();
        optionList.addAll(Arrays.asList("-d", project.getBuildTargetDir()));
        JavaCompiler.CompilationTask task
                = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);
        try {
            boolean ok = task.call();
            log("files compile status " + ok);
            diagnostics.getDiagnostics().stream().forEach(System.out::println);
            if (!ok) {
                throw new RuntimeException("error compiling file");
            }
            fileManager.close();
        } catch (Exception ex) {
            //  getLog().error(ex);
            ex.printStackTrace();
        }
        if(!classLoaderInit)
        {
            addSoftwareLibrary(new File(project.getBuildTargetDir()));
            classLoaderInit = true;
        }
    }

    protected void log(String log) {
        System.out.println(log);
    }

    private  void addSoftwareLibrary(File file) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(this.getClass().getClassLoader(), new Object[]{file.toURI().toURL()});
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException ex) {
            Logger.getLogger(MavenCodeGenSpi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public abstract void execute();

    public static final class Project {

        private final String baseDir;
        private final String buildDir;
        private final String buildTargetDir;

        public Project(String baseDir, String buildDir, String buildTargetDir) {
            this.baseDir = baseDir;
            this.buildDir = buildDir;
            this.buildTargetDir = buildTargetDir+"/";
        }

        public String getBaseDir() {
            return baseDir;
        }

        public String getBuildDir() {
            return buildDir;
        }

        public String getBuildTargetDir() {
            return buildTargetDir;
        }

        public String getSourceDir()
        {
            return baseDir+"/src/main/java";
        }
        @Override
        public String toString() {
            return "Project{" + "baseDir=" + baseDir + ", buildDir=" + buildDir + ", buildTargetDir=" + buildTargetDir + '}';
        }

    }

}
