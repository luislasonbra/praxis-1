/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.video.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.impl.PortListenerSupport;
import net.neilcsmith.praxis.video.VideoPort;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.Blit;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoInputPort extends VideoPort.Input {
    
    private final static Logger LOG = Logger.getLogger(DefaultVideoInputPort.class.getName());
    private final static int MAX_CONNECTIONS = 8;

    private final VideoPipe sink;
    private final List<VideoPort.Output> connections;
    private final PortListenerSupport pls;
    
    private VideoPipe portSink;
    private Mixer mixer;

    @Deprecated
    public DefaultVideoInputPort(Component host, VideoPipe sink) {
        this(sink);
    }

    public DefaultVideoInputPort(VideoPipe sink) {
        if (sink == null) {
            throw new NullPointerException();
        }
        this.sink = sink;
        this.portSink = sink;
        connections = new ArrayList<Output>(MAX_CONNECTIONS);
        pls = new PortListenerSupport(this);
    }

    public void disconnectAll() {
        for (VideoPort.Output connection : getConnections()) {
            disconnect(connection);
        }
    }

    public VideoPort.Output[] getConnections() {
        return connections.toArray(new VideoPort.Output[connections.size()]);
    }

    @Override
    protected void addVideoOutputPort(VideoPort.Output port, VideoPipe source) throws PortConnectionException {        
         if (connections.contains(port)) {
            throw new PortConnectionException();
        }
        if (connections.size() == 1) {
            switchToMultichannel();
        }
        try {
            portSink.addSource(source);
            connections.add(port);
            pls.fireListeners();
        } catch (Exception ex) {
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
            throw new PortConnectionException();
        }
    }

    @Override
    protected void removeVideoOutputPort(VideoPort.Output port, VideoPipe source) {
         if (connections.remove(port)) {
            portSink.removeSource(source);
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
            pls.fireListeners();
        }
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

     private void switchToMultichannel() {
        if (portSink == mixer) {
            return;
        }
        LOG.fine("VideoInput switching to multichannel");
        VideoPipe[] sources = removeSources(sink);
        try {
            if (mixer == null) {
                mixer = new Mixer(MAX_CONNECTIONS); // @TODO make channels configurable
            }
            sink.addSource(mixer);
            for (VideoPipe source : sources) {
                mixer.addSource(source);
            }
            portSink = mixer;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error converting port to multi channel", ex);
            removeSources(mixer);
            removeSources(sink);
            connections.clear();
            pls.fireListeners();
        }
    }

    private void switchToSingleChannel() {
        if (portSink == sink) {
            return;
        }
        LOG.fine("VideoInput switching to single channel");
        VideoPipe[] sources = removeSources(mixer);
        try {
            sink.removeSource(mixer);
            for (VideoPipe source : sources) {
                sink.addSource(source);
            }
            portSink = sink;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error converting port to single channel", ex);
            removeSources(sink);
            removeSources(mixer);
            connections.clear();
            pls.fireListeners();
        }

    }

    private VideoPipe[] removeSources(VideoPipe sink) {
        VideoPipe[] sources = new VideoPipe[sink.getSourceCount()];
        for (int i=0; i<sources.length; i++) {
            sources[i] = sink.getSource(i);
        }
        for (VideoPipe source : sources) {
            sink.removeSource(source);
        }
        return sources;
    }
    
    private static class Mixer extends MultiInOut {

        private Blit blit;

        private Mixer(int maxInputs) {
            super(maxInputs, 1);
            blit = new Blit();
            blit.setBlendMode(BlendMode.Add);
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int index, boolean rendering) {
            if (!rendering) {
                return;
            }

            if (inputs.length == 0) {
                output.clear();
                return;
            }

            for (int i = 0; i < inputs.length; i++) {
                Surface input = inputs[i];
                assert input != output;
                if (i == 0) {
                    output.copy(input);
                } else {
                    output.process(blit, input);
                }
                input.release();
            }
        }

    }

}
