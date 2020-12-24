package com.cloudimpl.code.generator;

/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author nuwansa
 */
@Mojo(name = "CodeGenerator",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresOnline = false, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = false)

public class CodeGenerator extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     */
    @Parameter(defaultValue = "${project}", required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    protected File projectBuildDir;

    @Parameter(defaultValue = "${project.basedir}/", required = true)
    protected File projectBaseDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    protected String projectTargetDirectory;

    private Map<Class, Reflector> providers = new HashMap<>();
    private List<String> runtimeClasspathElements;

    public static void main(String[] args) {
        new CodeGenerator().init();
    }

    private void init() {
        if (this.project != null) {

            try {
//                if (this.compiledFidxName == null) {
//                    this.compiledFidxName = fidxName + "Compiled";
//                }
//                if (this.compiledMsgidxName == null) {
//                    this.compiledMsgidxName = msgIdxName + "Compiled";
//                }
                String regexPath = ".*(com).*(cloudimpl).*(codegen-lib).*";
                //     this.project.addCompileSourceRoot(this.xbeOutputFolder.getAbsolutePath());
                //this.project.addCompileSourceRoot(this.projectSourceDirectory.getAbsolutePath());
                runtimeClasspathElements = project.getRuntimeClasspathElements();
                runtimeClasspathElements.addAll(project.getCompileClasspathElements());

//                        .filter(s -> s.matches(regexPath))
//                        .findFirst();
//                if (msgLibPath.isPresent()) {
//                    runtimeClasspathElements.add(msgLibPath.get());
//                }
//                classLoader = getClassLoader();
                ClassLoader classLoader = getClassLoader();
                this.project.getProperties().entrySet().forEach(e -> {
                    if (e.getKey().equals("com.cloudimpl.codegen.provider")) {
                        getLog().info("provider :" + e.getValue() + " loaded");
                        Class<?> providerClass = getRuntimeClass(classLoader, (String) e.getValue());
                        Reflector ref = new Reflector(providerClass);
                        providers.put(providerClass, ref);
                    }
                });

                runtimeClasspathElements.forEach(s -> {
                    providers.values().forEach(prv -> prv.addDependacyPath(s, true));
                });
                project.getArtifacts()
                        .stream()
                        .map(s -> {
                        //    System.out.println("xxxx : " + s.getFile().getAbsolutePath());
                            return s.getFile().getAbsolutePath();
                        }).forEach(p -> {
                    providers.values().forEach(prv -> prv.addDependacyPath(p, false));
                });

                this.project.getProperties().entrySet().forEach(e -> {
                    providers.values().forEach(p -> p.addProperty(e.getKey().toString(), e.getValue().toString()));
                });
                this.providers.values().forEach(p -> p.setProject(projectBaseDirectory.getAbsolutePath(),
                        projectBuildDir.getAbsolutePath(), projectTargetDirectory));
            } catch (DependencyResolutionRequiredException ex) {
                Logger.getLogger(CodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.providers.entrySet().forEach(e -> {
            if (!e.getValue().getCodeGenFolder().exists() && !e.getValue().getCodeGenFolder().mkdirs()) {
                getLog().error(e.getKey().getName() + ":Could not create error directory!");
            }
            cleanGeneratedCodes(e.getValue().getCodeGenFolder());
        });

    }

    private void cleanGeneratedCodes(File directory) {
        File[] files = directory.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                cleanGeneratedCodes(f);
            } else {
                f.delete();
            }
        }
    }

    private ClassLoader getClassLoader() {

        URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
        for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            try {
                String element = (String) runtimeClasspathElements.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(CodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
                Thread.currentThread().getContextClassLoader());
        return newLoader;

    }

    private void compileFiles(List<File> javaFiles) {
        if (javaFiles.size() == 0) {
            return;
        }
        // File[] javaFiles = new File[]{new File(javaFile.getAbsolutePath())};
        getLog().info("compiling files: " + javaFiles.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits
                = fileManager.getJavaFileObjectsFromFiles(javaFiles);

        List<String> optionList = new ArrayList<>();
//        if (msgLibPath.isPresent()) {
//            optionList.addAll(Arrays.asList("-classpath", msgLibPath.get()));
//        }
        File targetDir = new File(projectTargetDirectory);
        targetDir.mkdir();
        optionList.addAll(Arrays.asList("-d", projectTargetDirectory));
        JavaCompiler.CompilationTask task
                = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);
        try {
            boolean ok = task.call();
            getLog().info("files compile status " + ok);
            diagnostics.getDiagnostics().stream().forEach(System.out::println);
            if (!ok) {
                throw new RuntimeException("error compiling file");
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private <T> Class<T> getRuntimeClass(ClassLoader classLoader, String classFullName) {
        int i = 0;
        //while (i < 3) {
        try {
            if (classLoader == null) {
                classLoader = getClassLoader();
            }
            return (Class<T>) classLoader.loadClass(classFullName);
        } catch (ClassNotFoundException ex) {
            getLog().error(ex);
            throw new RuntimeException(ex);
            //  return null;
        }
        // i++;
        //  }
        //  return null;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
            providers.values().forEach(p -> p.execute());
        } catch (Exception ex) {
            getLog().error(ex);
            throw ex;
        }

    }
}
