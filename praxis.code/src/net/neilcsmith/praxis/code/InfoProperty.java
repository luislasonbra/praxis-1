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
package net.neilcsmith.praxis.code;

import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentInterface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class InfoProperty implements Control {

    private CodeContext<?> context;

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        Call.Type type = call.getType();
        if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
            router.route(Call.createReturnCall(call, context.getInfo()));
        } else {
            // do nothing?
        }
    }

    @Override
    public ControlInfo getInfo() {
        return ComponentInterface.INFO_INFO;

    }

    public static class Descriptor extends ControlDescriptor {

        private final InfoProperty control;

        public Descriptor(int index) {
            super(ComponentInterface.INFO, Category.Internal, index);
            control = new InfoProperty();
        }

        @Override
        public ControlInfo getInfo() {
            return control.getInfo();
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            control.context = context;
        }

        @Override
        public Control getControl() {
            return control;
        }

    }

}
