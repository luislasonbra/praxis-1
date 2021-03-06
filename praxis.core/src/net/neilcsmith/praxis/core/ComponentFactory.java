/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.core;

import net.neilcsmith.praxis.core.interfaces.ComponentFactoryService;
import net.neilcsmith.praxis.core.interfaces.RootFactoryService;

/**
 *
 * @author Neil C Smith
 */
public interface ComponentFactory {

    public ComponentType[] getComponentTypes();

    public ComponentType[] getRootComponentTypes();

    public MetaData<? extends Component> getMetaData(ComponentType type);

    public MetaData<? extends Root> getRootMetaData(ComponentType type);
    
    public default Component createComponent(ComponentType type) throws ComponentInstantiationException {
        throw new ComponentInstantiationException();
    }

    public default Root createRootComponent(ComponentType type) throws ComponentInstantiationException {
        throw new ComponentInstantiationException();
    }
    
    public default Class<? extends ComponentFactoryService> getFactoryService() {
        return ComponentFactoryService.class;
    }
    
    public default Class<? extends RootFactoryService> getRootFactoryService() {
        return RootFactoryService.class;
    }
    
    public static abstract class MetaData<T> {

        @Deprecated
        public abstract Class<T> getComponentClass();

        @Deprecated
        public boolean isTest() {
            return false;
        }

        public boolean isDeprecated() {
            return false;
        }

        public ComponentType getReplacement() {
            return null;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
