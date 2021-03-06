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
package net.neilcsmith.praxis.tinkerforge;

import net.neilcsmith.praxis.code.AbstractComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class TFComponents implements ComponentFactoryProvider {
    
    private final static Factory instance = new Factory();

    @Override
    public ComponentFactory getFactory() {
        return instance;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            
            add("tinkerforge:ambient-light", "resources/ambient_light.pxj");
            add("tinkerforge:analog-in", "resources/analog_in.pxj");
            add("tinkerforge:analog-out", "resources/analog_out.pxj");
            add("tinkerforge:barometer", "resources/barometer.pxj");
            add("tinkerforge:distance-ir", "resources/distance_ir.pxj");
            add("tinkerforge:dual-relay", "resources/dual_relay.pxj");
            add("tinkerforge:io16", "resources/io16.pxj");
            add("tinkerforge:joystick", "resources/joystick.pxj");
            add("tinkerforge:lcd20x4", "resources/lcd20x4.pxj");
            add("tinkerforge:linear-poti", "resources/linear_poti.pxj");
            add("tinkerforge:rotary-poti", "resources/rotary_poti.pxj");
            add("tinkerforge:servo", "resources/servo.pxj");
            add("tinkerforge:temperature", "resources/temperature.pxj");
            add("tinkerforge:temperature-ir", "resources/temperature_ir.pxj");
                      
            add(data(new TFCodeFactory("tinkerforge:custom")));

        }

        private void add(String type, String sourceFile) {
            add(data(new TFCodeFactory(type, source(sourceFile))));
        }
        
    }
}
