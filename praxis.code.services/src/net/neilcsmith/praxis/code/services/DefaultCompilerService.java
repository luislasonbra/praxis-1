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
package net.neilcsmith.praxis.code.services;

import java.io.File;
import java.net.URI;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import net.neilcsmith.praxis.code.CodeCompilerService;
import net.neilcsmith.praxis.compiler.ClassBodyCompiler;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.compiler.JavaCompilerProvider;
import net.neilcsmith.praxis.compiler.MessageHandler;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PBytes;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.SimpleControl;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DefaultCompilerService extends AbstractRoot {
    
    static final String EXT_CLASSPATH = "ext-classpath";
    
    private final JavaCompiler compiler;
    private final Set<File> libJARs;
    
    public DefaultCompilerService() {
        super(EnumSet.noneOf(Caps.class));
        registerControl(CodeCompilerService.COMPILE, new CompileControl());
        registerControl("add-libs", new AddLibsControl());
        registerInterface(CodeCompilerService.class);
        JavaCompilerProvider compilerProvider = Lookup.SYSTEM.get(JavaCompilerProvider.class);
        if (compilerProvider != null) {
            compiler = compilerProvider.getJavaCompiler();
        } else {
            throw new RuntimeException("No compiler found");
        }
        libJARs = new LinkedHashSet<>();
    }
    
    private class CompileControl extends SimpleControl {
        
        private CompileControl() {
            super(CodeCompilerService.COMPILE_INFO);
        }
        
        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            PMap map = PMap.coerce(args.get(0));
            String code = map.getString(CodeCompilerService.KEY_CODE, "");
            ClassBodyContext<?> cbc = getClassBodyContext(map);
            LogBuilder log = new LogBuilder(LogLevel.WARNING);
            Map<String, byte[]> classFiles
                    = ClassBodyCompiler.create(cbc)
                            .setCompiler(compiler)
                            .addMessageHandler(new LogMessageHandler(log))
                            .extendClasspath(libJARs)
                            .compile(code);
            PMap classes = convertClasses(classFiles);
            PMap response = PMap.create(CodeCompilerService.KEY_CLASSES, classes,
                    CodeCompilerService.KEY_LOG, PArray.valueOf(log.toCallArguments()),
                    EXT_CLASSPATH, convertClasspath());
            return CallArguments.create(response);
        }
        
        private ClassBodyContext<?> getClassBodyContext(PMap map) throws ClassNotFoundException,
                InstantiationException, IllegalAccessException {
            String cbcClass = map.getString(CodeCompilerService.KEY_CLASS_BODY_CONTEXT, null);
            return (ClassBodyContext<?>) Class.forName(cbcClass, true, Thread.currentThread().getContextClassLoader()).newInstance();
        }
        
        private LogLevel getLogLevel(PMap map) {
            String level = map.getString(CodeCompilerService.KEY_LOG_LEVEL, null);
            if (level != null) {
                return LogLevel.valueOf(level);
            } else {
                return LogLevel.ERROR;
            }
        }
        
        private PMap convertClasses(Map<String, byte[]> classes) {
            PMap.Builder bld = PMap.builder(classes.size());
            classes.entrySet().stream().forEach((type) -> {
                bld.put(type.getKey(), PBytes.valueOf(type.getValue()));
            });
            return bld.build();
        }
        
        private PArray convertClasspath() {
            return libJARs.stream()
                    .map(f -> PResource.valueOf(f.toURI()))
                    .collect(PArray.collector());
        }
        
    }
    
    private class AddLibsControl extends SimpleControl {
        
        private AddLibsControl() {
            super(null);
        }
        
        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            PArray libs = PArray.from(args.get(0)).orElseThrow(IllegalArgumentException::new);
            Set<File> jars = libs.stream()
                    .map(v -> PResource.from(v).orElseThrow(IllegalArgumentException::new))
                    .map(r -> jarFile(r))
                    .collect(Collectors.toSet());
            libJARs.addAll(jars);
            return CallArguments.create(
                    libJARs.stream()
                            .map(f -> PResource.valueOf(f.toURI()))
                            .collect(PArray.collector())
            );
        }
        
        private File jarFile(PResource res) {
            List<URI> uris = res.resolve(getLookup());
            return uris.stream()
                    .filter(u -> "file".equals(u.getScheme()))
                    .map(File::new)
                    .filter(f -> f.exists() && f.getName().endsWith(".jar"))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid library : " + res));
        }
        
    }
    
    private static class LogMessageHandler implements MessageHandler {
        
        private final LogBuilder log;
        
        private LogMessageHandler(LogBuilder log) {
            this.log = log;
        }
        
        @Override
        public void handleError(String msg) {
            log.log(LogLevel.ERROR, msg);
        }
        
        @Override
        public void handleWarning(String msg) {
            log.log(LogLevel.WARNING, msg);
        }
        
    }
    
}
