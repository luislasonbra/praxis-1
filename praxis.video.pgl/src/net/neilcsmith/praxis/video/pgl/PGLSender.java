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
package net.neilcsmith.praxis.video.pgl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.Surface;
import processing.core.PGraphics;

/**
 *
 * @author neilcsmith
 */
public final class PGLSender extends AbstractExecutionContextComponent {
    
    private final Delegator delegator;
    private final PGLTextureSharer textureSharer;

    private PGLTextureSharer.Sender sender;
    private String serverID = "";
    private boolean active;

    public PGLSender() {
        delegator = new Delegator();
        textureSharer = PGLTextureSharer.find().orElse(null);
        registerPort(Port.IN, new DefaultVideoInputPort(delegator));
        registerPort(Port.OUT, new DefaultVideoOutputPort(delegator));
        registerControl("server-id", StringProperty.builder().binding(new StringProperty.Binding() {
            @Override
            public void setBoundValue(long time, String value) {
                if (active) {
                    throw new UnsupportedOperationException("Can't set server ID while active");
                }
                serverID = value;
            }

            @Override
            public String getBoundValue() {
                return serverID;
            }
        }).build());
        TriggerControl start = TriggerControl.create(time -> active = true);
        TriggerControl stop = TriggerControl.create((long time) -> {
            PGLSender.this.handleDispose();
        });
        registerControl("start", start);
        registerControl("stop", stop);
        registerPort("start", start.createPort());
        registerPort("stop", stop.createPort());
    }

    @Override
    public void stateChanged(ExecutionContext source) {
        if (source.getState() != ExecutionContext.State.ACTIVE) {
            handleDispose();
        }
    }

    @Override
    public void hierarchyChanged() {
        handleDispose();
        super.hierarchyChanged();
    }
    
    private void handleProcess(PGLSurface surface) {
        if (active) {
            if (sender == null) {
                initSender(surface.getContext());
                if (sender == null) {
                    active = false;
                    return;
                }
            }
            PGraphics pg = surface.getGraphics();
            pg.endDraw();
            sender.sendFrame(pg);
        }
    }
    
    private void initSender(PGLContext context) {
        if (textureSharer != null) {
            try {
                sender = textureSharer.createSender(context, serverID);
            } catch (Exception ex) {
                Logger.getLogger(PGLSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleDispose() {
        active = false;
        if (sender != null) {
            sender.dispose();
            sender = null;
        }
    }

    private class Delegator extends SingleInOut {

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                if (surface instanceof PGLSurface) {
                    handleProcess((PGLSurface) surface);
                }
            }
        }

    }

}
