/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.audio.code;

import net.neilcsmith.praxis.code.CodeUtils;
import net.neilcsmith.praxis.compiler.ClassBodyContext;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioBodyContext extends ClassBodyContext<AudioCodeDelegate> {
    
    public final static String TEMPLATE =
            CodeUtils.load(AudioBodyContext.class, "resources/audio_template.pxj");
    
    private final static String[] IMPORTS = CodeUtils.join(
            CodeUtils.defaultImports(), new String[]{
                "net.neilcsmith.praxis.audio.code.userapi.*",
                "static net.neilcsmith.praxis.audio.code.userapi.AudioConstants.*"
            });
    
    public AudioBodyContext() {
        super(AudioCodeDelegate.class);
    }
    
    @Override
    public String[] getDefaultImports() {
        return IMPORTS.clone();
    }
    
}
