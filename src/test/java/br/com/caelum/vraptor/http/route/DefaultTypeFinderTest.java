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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Map;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.core.DefaultReflectionProvider;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.http.ParanamerNameProvider;
import br.com.caelum.vraptor.http.route.DefaultTypeFinder;

public class DefaultTypeFinderTest {

	private ParameterNameProvider provider;
	
	@Before
	public void setup() {
		provider = new ParanamerNameProvider();
	}

	public static class AController {
		public void aMethod(Bean bean, String path) {

		}
		public void otherMethod(BeanExtended extended) {

		}
	}

	public static class Bean {

		public Bean2 getBean2() {
			return new Bean2();
		}
	}

	public static class BeanExtended extends Bean2 {

	}
	public static class Bean2 {
		public Integer getId() {
			return 1;
		}
	}
	@Test
	public void shouldGetTypesCorrectly() throws Exception {

		final Method method = new Mirror().on(AController.class).reflect().method("aMethod").withArgs(Bean.class, String.class);
		
		DefaultTypeFinder finder = new DefaultTypeFinder(provider, new DefaultReflectionProvider());
		Map<String, Class<?>> types = finder.getParameterTypes(method, new String[] {"bean.bean2.id", "path"});

		assertEquals(Integer.class, types.get("bean.bean2.id"));
		assertEquals(String.class, types.get("path"));
	}
	@Test
	public void shouldGetTypesCorrectlyOnInheritance() throws Exception {
		final Method method = new Mirror().on(AController.class).reflect().method("otherMethod").withArgs(BeanExtended.class);
		
		DefaultTypeFinder finder = new DefaultTypeFinder(provider, new DefaultReflectionProvider());
		Map<String, Class<?>> types = finder.getParameterTypes(method, new String[] {"extended.id"});

		assertEquals(Integer.class, types.get("extended.id"));
	}
}
