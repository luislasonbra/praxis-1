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
package net.neilcsmith.praxis.code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.interfaces.ComponentFactoryService;
import net.neilcsmith.praxis.impl.InstanceLookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractComponentFactory implements ComponentFactory {

    private final Map<ComponentType, MetaData> componentMap;

    protected AbstractComponentFactory() {
        componentMap = new LinkedHashMap<>();
    }

    @Override
    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    @Override
    public ComponentType[] getRootComponentTypes() {
        return new ComponentType[0];
    }

    @Override
    public ComponentFactory.MetaData<? extends Component> getMetaData(ComponentType type) {
        return componentMap.get(type);
    }

    @Override
    public ComponentFactory.MetaData<? extends Root> getRootMetaData(ComponentType type) {
        return null;
    }

    @Override
    public Class<? extends ComponentFactoryService> getFactoryService() {
        return CodeComponentFactoryService.class;
    }

    protected void add(Data info) {
        componentMap.put(info.factory.getComponentType(), info.toMetaData());
    }

    protected Data data(CodeFactory<?> factory) {
        return new Data(factory);
    }

    protected String source(String location) {
        return CodeUtils.load(getClass(), location);
    }

    private static class MetaData extends ComponentFactory.MetaData<Component> {

        private final CodeFactory<?> factory;
        private final boolean test;
        private final boolean deprecated;
        private final ComponentType replacement;
        private final Lookup lookup;

        private MetaData(
                CodeFactory<?> factory,
                boolean test,
                boolean deprecated,
                ComponentType replacement,
                Lookup lookup) {
            this.factory = factory;
            this.test = test;
            this.deprecated = deprecated;
            this.replacement = replacement;
            this.lookup = lookup;
        }

        @Override
        @Deprecated
        public Class<Component> getComponentClass() {
            return Component.class;
        }

        @Override
        public boolean isTest() {
            return test;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public ComponentType getReplacement() {
            return replacement;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        private CodeFactory<?> getCodeFactory() {
            return factory;
        }

    }

    public static class Data {

        private final CodeFactory<?> factory;
        private final List<Object> lookupList;
        private boolean test;
        private boolean deprecated;
        private ComponentType replacement;

        private Data(CodeFactory<?> factory) {
            this.factory = factory;
            lookupList = new ArrayList<>();
            lookupList.add(factory);
        }

        public Data test() {
            test = true;
            return this;
        }

        public Data deprecated() {
            deprecated = true;
            return this;
        }

        public Data replacement(String type) {
            replacement = ComponentType.create(type);
            deprecated = true;
            return this;
        }

        public Data add(Object obj) {
            lookupList.add(obj);
            return this;
        }

        private MetaData toMetaData() {
            return new MetaData(factory, test, deprecated, replacement,
                    InstanceLookup.create(lookupList.toArray()));
        }
    }
}
