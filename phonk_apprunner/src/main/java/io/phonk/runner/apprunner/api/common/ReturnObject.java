/*
 * Part of Phonk http://www.phonk.io
 * A prototyping platform for Android devices
 *
 * Copyright (C) 2013 - 2017 Victor Diaz Barrales @victordiaz (Protocoder)
 * Copyright (C) 2017 - Victor Diaz Barrales @victordiaz (Phonk)
 *
 * Phonk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Phonk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Phonk. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.phonk.runner.apprunner.api.common;

import org.mozilla.javascript.Scriptable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.phonk.runner.base.utils.GSONUtil;

// http://stackoverflow.com/questions/7519399/how-to-convert-java-map-to-a-basic-javascript-object

/**
 * Object returned from java to js using a callback
 * @see io.phonk.runner.apprunner.api.common.ReturnInterface#event(ReturnObject)
 */
public class ReturnObject implements Scriptable, Map {

    public final Map map;

    public ReturnObject() {
        this.map = new HashMap();
    }

    public ReturnObject(Object obj) {
        this();
        map.put("obj", obj);
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map m) {
        map.putAll(m);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection values() {
        return map.values();
    }

    @Override
    public void delete(String name) {
        map.remove(name);
    }

    @Override
    public void delete(int index) {
        map.remove(index);
    }

    @Override
    public Object get(String name, Scriptable start) {
        return map.get(name);
    }

    @Override
    public Object get(int index, Scriptable start) {
        return map.get(index);
    }

    @Override
    public String getClassName() {
        return map.getClass().getName();
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return toString();
    }

    @Override
    public Object[] getIds() {
        Object[] res = new Object[map.size()];
        int i = 0;
        for (Object k : map.keySet()) {
            res[i] = k;
            i++;
        }
        return res;
    }

    @Override
    public Scriptable getParentScope() {
        return null;
    }

    @Override
    public Scriptable getPrototype() {
        return null;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return map.containsKey(name);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return map.containsKey(index);
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return false;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        map.put(name, value);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        map.put(index, value);
    }

    @Override
    public void setParentScope(Scriptable parent) {
    }

    @Override
    public void setPrototype(Scriptable prototype) {
    }

    @Override
    public String toString() {
        return GSONUtil.getInstance().getGson().toJson(this);
    }
}