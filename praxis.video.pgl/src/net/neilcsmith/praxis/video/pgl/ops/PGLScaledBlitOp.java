/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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

import java.awt.Rectangle;
import java.util.logging.Level;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.ScaledBlit;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLScaledBlitOp extends AbstractBlitOp {
    
    private Rectangle rect;

    PGLScaledBlitOp() {
        super(ScaledBlit.class);
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            if (process((ScaledBlit) op, output, inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(ScaledBlit blit, PGLSurface dst, Surface src) {
        try {
            BlendMode mode = blit.getBlendMode();
            if (canProcessDirect(mode)) {
                PGLGraphics g = dst.getGraphics();
                PImage img = dst.getContext().asImage(src);
                g.beginDraw();
                setupBlending(g, mode, (float) blit.getOpacity());
                
                Rectangle bnds = blit.getSourceRegion(rect);      
                int srcX = bnds == null ? 0 : bnds.x;
                int srcW = bnds == null ? src.getWidth() : bnds.width;          
                int srcY = bnds == null ? 0 : bnds.y;
                int srcH = bnds == null ? src.getHeight() : bnds.height;
                
                bnds = blit.getDestinationRegion(rect);
                int dstW = bnds == null ? dst.getWidth() : bnds.width;
                int dstX = bnds == null ? 0 : bnds.x;
                int dstH = bnds == null ? dst.getHeight() : bnds.height;
                int dstY = bnds == null ? 0 : bnds.y;
                
                g.image(img, dstX, dstY, dstW, dstH,
                        srcX, srcY, srcX + srcW, srcY + srcH);
                return true;
            }
        } catch (Exception ex) {
            // fall through
            LOG.log(Level.FINE, "Scaled blit threw exception", ex);
        }
        return false;
    }
}
