/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.caelum.vraptor.serialization.xstream;

import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;

import javax.enterprise.inject.Vetoed;

import com.google.common.base.Supplier;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import br.com.caelum.vraptor.core.ReflectionProvider;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.serialization.Serializee;
import br.com.caelum.vraptor.validator.Message;

@Vetoed
public class VRaptorClassMapper extends MapperWrapper {

	private final Supplier<TypeNameExtractor> extractor;
	private final Supplier<Serializee> serializee;
	private final Supplier<ReflectionProvider> reflectionProvider;

	public VRaptorClassMapper(Mapper wrapped, Supplier<TypeNameExtractor> supplier, Supplier<Serializee> serializee,
			Supplier<ReflectionProvider> reflectionProvider) {
		super(wrapped);
		this.extractor = supplier;
		this.serializee = serializee;
		this.reflectionProvider = reflectionProvider;
	}

	static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive()
			|| type.isEnum()
			|| Number.class.isAssignableFrom(type)
			|| type.equals(String.class)
			|| Date.class.isAssignableFrom(type)
			|| Calendar.class.isAssignableFrom(type)
			|| Boolean.class.equals(type)
			|| Character.class.equals(type);
	}

	@Override
	public boolean shouldSerializeMember(Class definedIn, String fieldName) {
		for (Entry<String, Class<?>> include : getSerializee().getIncludes().entries()) {
			if (isCompatiblePath(include, definedIn, fieldName)) {
				return true;
			}
		}
		for (Entry<String, Class<?>> exclude : getSerializee().getExcludes().entries()) {
			if (isCompatiblePath(exclude, definedIn, fieldName)) {
				return false;
			}
		}
		
		boolean should = super.shouldSerializeMember(definedIn, fieldName);
		if (!getSerializee().isRecursive())
			should = should && isPrimitive(reflectionProvider.get().getField(definedIn, fieldName).getType());
		return should;
	}

	private static boolean isCompatiblePath(Entry<String, Class<?>> path, Class definedIn, String fieldName) {
		return path.getValue().equals(definedIn) && (path.getKey().equals(fieldName) || path.getKey().endsWith("." + fieldName));
	}

	@Override
	public String serializedClass(Class type) {
		if (type == null) return super.serializedClass(type);
		if (Message.class.isAssignableFrom(type)) {
			return "message";
		}
		String superName = super.serializedClass(type);
		if (type.getName().equals(superName)) {
			return extractor.get().nameFor(type);
		}
		return superName;
	}
	
	public Serializee getSerializee() {
		return serializee.get();
	}
}
