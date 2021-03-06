/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.compiler;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;

/**
 *
 * @author Neil C Smith
 */
public class ClassBodyCompiler {

    public final static String DEFAULT_CLASS_NAME = "$";

    private final ClassBodyContext<?> classBodyContext;
    private final Set<File> extClasspath;
    private final String defClasspath;

    private MessageHandler messageHandler;
    private JavaCompiler compiler;

    private ClassBodyCompiler(ClassBodyContext<?> classBodyContext) {
        this.classBodyContext = classBodyContext;
        this.extClasspath = new LinkedHashSet<>();
        this.defClasspath = System.getProperty("env.class.path", "");
    }

    public ClassBodyCompiler addMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public ClassBodyCompiler extendClasspath(Set<File> libs) {
        extClasspath.addAll(libs);
        return this;
    }

    public ClassBodyCompiler setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
        return this;
    }

    public Map<String, byte[]> compile(String code) throws CompilationException {
        try {
            ClassBodyEvaluator cbe = new ClassBodyEvaluator();
            cbe.setCompiler(compiler);
            cbe.setExtendedClass(classBodyContext.getExtendedClass());
            cbe.setImplementedInterfaces(classBodyContext.getImplementedInterfaces());
            cbe.setDefaultImports(classBodyContext.getDefaultImports());
            if (messageHandler != null) {
                cbe.setMessageHandler(messageHandler);
            }
            cbe.setOptions(Arrays.asList("-Xlint:all", "-classpath", buildClasspath()));
            cbe.cook(new StringReader(code));
            return cbe.getCompiledClasses();
        } catch (CompilationException ex) {
            throw new CompilationException(ex);
        } catch (Exception ex) {
            throw new CompilationException(ex);
        }
    }
    private String buildClasspath() {
        if (extClasspath.isEmpty()) {
            return defClasspath;
        } else {
            return extClasspath.stream()
                    .map(f -> f.getAbsolutePath())
                    .collect(Collectors.joining(File.pathSeparator, 
                            "", File.pathSeparator + defClasspath));
        }
    }

    @Deprecated
    public <T> Class<T> compile(ClassBodyContext<T> context, String code)
            throws CompilationException {
        return compile(context, null, code);
    }

    @Deprecated
    public <T> Class<T> compile(ClassBodyContext<T> context,
            MessageHandler messageHandler, String code)
            throws CompilationException {
        try {
            ClassBodyEvaluator cbe = new ClassBodyEvaluator();
            cbe.setExtendedClass(context.getExtendedClass());
            cbe.setImplementedInterfaces(context.getImplementedInterfaces());
            cbe.setDefaultImports(context.getDefaultImports());
            if (messageHandler != null) {
                cbe.setMessageHandler(messageHandler);
            }
            cbe.cook(new StringReader(code));
            return (Class<T>) cbe.getClazz();
        } catch (CompilationException ex) {
            throw new CompilationException(ex);
        } catch (Exception ex) {
            throw new CompilationException(ex);
        }
    }

    public static ClassBodyCompiler create(ClassBodyContext<?> classBodyContext) {
        return new ClassBodyCompiler(classBodyContext);
    }

    @Deprecated
    public static ClassBodyCompiler getDefault() {
        return new ClassBodyCompiler(null);
    }

}
