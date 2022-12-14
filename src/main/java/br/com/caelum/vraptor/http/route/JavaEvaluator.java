/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.caelum.vraptor.http.route;

import java.lang.reflect.Array;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.Iterables;

import br.com.caelum.vraptor.VRaptorException;
import br.com.caelum.vraptor.core.ReflectionProvider;

/**
 * Evaluates expressions in order to access values.
 *
 * @author guilherme silveira
 *
 */
@ApplicationScoped
public class JavaEvaluator implements Evaluator {

	private final ReflectionProvider reflectionProvider;

	/**
	 * @deprecated CDI eyes only
	 */
	protected JavaEvaluator() {
		this(null);
	}

	@Inject
	public JavaEvaluator(ReflectionProvider reflectionProvider) {
		this.reflectionProvider = reflectionProvider;
	}

	@Override
	public Object get(Object root, String path) {
		if (root == null) {
			return null;
		}
		String[] paths = path.split("[\\]\\.]");
		Object current = root;
		for (int i = 1; i < paths.length; i++) {
			try {
				current = navigate(current, paths[i]);
			} catch (Exception e) {
				throw new VRaptorException("Unable to evaluate expression " + path, e);
			}
			if (current == null) {
				return "";
			}
		}
		return current;
	}

	private Object navigate(Object current, String path) {
		int index = path.indexOf("[");
		int position = -1;
		if (index != -1) {
			position = Integer.parseInt(path.substring(index + 1));
			path = path.substring(0, index);
		}
		Object instance = reflectionProvider.invokeGetter(current, path);
		if (index != -1) {
			instance = access(instance, position);
		}
		return instance;
	}

	private Object access(Object current, int position) {
		if (current.getClass().isArray()) {
			return Array.get(current, position);
		} else if (Collection.class.isAssignableFrom(current.getClass())) {
			return Iterables.get((Collection<?>) current, position);
		}
		throw new VRaptorException("Unable to access position of a" + current.getClass().getName() + ".");
	}
}
