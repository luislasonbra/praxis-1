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
package net.neilcsmith.praxis.script.commands;

import net.neilcsmith.praxis.script.impl.AbstractInlineCommand;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.Variable;
import net.neilcsmith.praxis.script.impl.VariableImpl;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class FileCmds implements CommandInstaller {

    private final static FileCmds INSTANCE = new FileCmds();

    private final static Command FILE = new FileCmd();
    private final static Command FILE_LIST = new FileListCmd();
    private final static Command FILE_NAMES = new FileNamesCmd();
    private final static Command CD = new CdCmd();
    private final static Command PWD = new PwdCmd();

    private FileCmds() {
    }

    public void install(Map<String, Command> commands) {
        commands.put("file", FILE);
        commands.put("file-list", FILE_LIST);
        commands.put("file-names", FILE_NAMES);
        commands.put("ls", FILE_NAMES);
        commands.put("cd", CD);
        commands.put("pwd", PWD);
    }

    public static FileCmds getInstance() {
        return INSTANCE;
    }

    private static URI getPWD(Namespace namespace) {
        Variable pwd = namespace.getVariable(Env.PWD);
        if (pwd != null) {
            try {
                return PResource.coerce(pwd.getValue()).value();
            } catch (ArgumentFormatException ex) {
                Logger.getLogger(FileCmds.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new File("").toURI();
    }

    private static URI resolve(Namespace namespace, String path) throws URISyntaxException {
        if (path.contains(":")) {
            try {
                URI uri = new URI(path);
                if (uri.isAbsolute()) {
                    return uri;
                }
            } catch (URISyntaxException ex) {
                // fall through?
            }
        }
        URI base = getPWD(namespace);
        URI uri = base.resolve(new URI(null, null, path, null));
        if ("file".equals(base.getScheme())) {
            uri = new File(uri).toURI();
        }
        return uri;
    }

    private static List<Path> listFiles(Namespace namespace) throws Exception {
        return listFiles(namespace, "", "*");
    }

    private static List<Path> listFiles(Namespace namespace, String globPath) throws Exception {
        int lastSlash = globPath.lastIndexOf("/");
        String path = "";
        if (lastSlash > 0) {
            path = globPath.substring(0, lastSlash);
        }
        String glob = "*";
        if (lastSlash < (globPath.length() - 1)) {
            glob = globPath.substring(lastSlash + 1);
        }
        return listFiles(namespace, path, glob);
    }

    private static List<Path> listFiles(Namespace namespace, String path, String glob) throws Exception {
        URI base = path.isEmpty() ? getPWD(namespace) : resolve(namespace, path);
        List<Path> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(base), glob)) {
            for (Path entry : stream) {
                list.add(entry);
            }
        }
        list.sort(Comparator.naturalOrder());
        return list;
    }

    private static class FileCmd extends AbstractInlineCommand {

        public CallArguments process(Env env, Namespace namespace, CallArguments args) throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                return CallArguments.create(PResource.valueOf(
                        resolve(namespace, args.get(0).toString())
                ));
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }
        }
    }

    private static class FileListCmd extends AbstractInlineCommand {

        @Override
        public CallArguments process(Env context, Namespace namespace, CallArguments args) throws ExecutionException {
            try {
                List<Path> list;
                if (args.getSize() == 2) {
                    list = listFiles(namespace, args.get(0).toString(), args.get(1).toString());
                } else if (args.getSize() == 1) {
                    list = listFiles(namespace, args.get(0).toString());
                } else {
                    list = listFiles(namespace);
                }
                
                List<PResource> ret = list.stream()
                        .map(path -> PResource.valueOf(path.toUri()))
                        .collect(Collectors.toList());
                
                return CallArguments.create(PArray.valueOf(ret));
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }

        }

    }

    private static class FileNamesCmd extends AbstractInlineCommand {

        public CallArguments process(Env env, Namespace namespace, CallArguments args) throws ExecutionException {
            try {
                List<Path> list;
                if (args.getSize() == 2) {
                    list = listFiles(namespace, args.get(0).toString(), args.get(1).toString());
                } else if (args.getSize() == 1) {
                    list = listFiles(namespace, args.get(0).toString());
                } else {
                    list = listFiles(namespace);
                }
                
                List<PString> ret = list.stream()
                        .map(path -> PString.valueOf(path.getFileName()))
                        .collect(Collectors.toList());
                
                return CallArguments.create(PArray.valueOf(ret));
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }
        }

    }
    
    private static class CdCmd extends AbstractInlineCommand {

        @Override
        public CallArguments process(Env context, Namespace namespace, CallArguments args) throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                URI uri = resolve(namespace, args.get(0).toString());
                if ("file".equals(uri.getScheme())) {
                    File d = new File(uri);
                    if (!d.isDirectory()) {
                        throw new ExecutionException("Not a valid directory");
                    }
                }
                PResource dir = PResource.valueOf(uri);
                Variable pwd = namespace.getVariable(Env.PWD);
                if (pwd != null) {
                    pwd.setValue(dir);
                } else {
                    pwd = new VariableImpl(dir);
                    namespace.addVariable(Env.PWD, pwd);
                }
                return CallArguments.create(dir);
            } catch (URISyntaxException ex) {
                throw new ExecutionException(ex);
            }
        }
        
    }
    
    private static class PwdCmd extends AbstractInlineCommand {

        @Override
        public CallArguments process(Env context, Namespace namespace, CallArguments args) throws ExecutionException {
            return CallArguments.create(PResource.valueOf(getPWD(namespace)));
        }
        
    }    
    
}
