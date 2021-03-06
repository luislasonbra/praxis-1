/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl.ops;

import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import static net.neilcsmith.praxis.video.render.ops.BlendMode.*;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class AbstractBlendOp extends PGLOp {

    protected AbstractBlendOp(Class<? extends SurfaceOp> opClass) {
        super(opClass);
    }

    boolean canProcessDirect(BlendMode mode) {
        switch (mode) {
            case Normal:
            case Add:
            case Sub:
            case Multiply:
                return true;
            default:
                return false;
        }
    }

    void setupBlending(PGLGraphics g, BlendMode mode) {
        switch (mode) {
            case Normal:
                g.blendMode(PGLGraphics.BLEND);
                break;
            case Add:
                g.blendMode(PGLGraphics.ADD);
                break;
            case Sub:
                g.blendMode(PGLGraphics.SUBTRACT);
                break;
            case Multiply:
                g.blendMode(PGLGraphics.MULTIPLY);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

}
