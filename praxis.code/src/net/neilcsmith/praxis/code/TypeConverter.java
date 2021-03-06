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
 *
 */
package net.neilcsmith.praxis.code;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class TypeConverter<T> {
    
    public abstract Argument toArgument(T value);
    
    public abstract T fromArgument(Argument value) throws ArgumentFormatException;
    
    public abstract Class<T> getType();
    
    public ArgumentInfo getInfo() {
        return ArgumentInfo.info();
    }
    
    public boolean isRealTimeSafe() {
        return false;
    }
    
    public Argument getDefaultArgument() {
        return PString.EMPTY;
    }
    
    public T getDefaultValue() {
        return null;
    }
    
    public static interface Provider {
        
        public <T> TypeConverter<T> getTypeConverter(Class<T> type, Annotation ... annotations);
        
    }
    
    public final static <T> TypeConverter<T> find(Class<T> type, Annotation ... annotations) {
        try {
            for (Provider p : Lookup.SYSTEM.getAll(Provider.class)) {
                TypeConverter converter = p.getTypeConverter(type, annotations);
                if (converter != null) {
                    return converter;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TypeConverter.class.getName())
                    .log(Level.SEVERE, "Exception looking for TypeConverter", ex);
        }
        return null;
    }
    
}
