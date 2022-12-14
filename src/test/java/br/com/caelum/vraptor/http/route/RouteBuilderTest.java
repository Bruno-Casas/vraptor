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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.controller.DefaultBeanClass;
import br.com.caelum.vraptor.controller.DefaultControllerMethod;
import br.com.caelum.vraptor.core.Converters;
import br.com.caelum.vraptor.core.DefaultReflectionProvider;
import br.com.caelum.vraptor.http.EncodingHandler;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.http.ParanamerNameProvider;
import br.com.caelum.vraptor.http.route.DefaultRouteBuilder;
import br.com.caelum.vraptor.http.route.DefaultTypeFinder;
import br.com.caelum.vraptor.http.route.JavaEvaluator;
import br.com.caelum.vraptor.http.route.Route;
import br.com.caelum.vraptor.http.route.RouteBuilder;
import br.com.caelum.vraptor.http.route.TypeFinder;
import br.com.caelum.vraptor.proxy.JavassistProxifier;
import br.com.caelum.vraptor.proxy.Proxifier;

public class RouteBuilderTest {

	private ParameterNameProvider provider;
	private @Mock Converters converters;
	private RouteBuilder builder;
	private ControllerMethod method;
	private Proxifier proxifier;
	private TypeFinder typeFinder;
	private @Mock EncodingHandler encodingHandler;

	public static class MyResource {

		public void method(String abc, Integer def, BigDecimal ghi) {

		}
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		provider = new ParanamerNameProvider();
		
		method = new DefaultControllerMethod(new DefaultBeanClass(MyResource.class), MyResource.class.getMethod(
				"method", String.class, Integer.class, BigDecimal.class));

		proxifier = new JavassistProxifier();
		typeFinder = new DefaultTypeFinder(provider, new DefaultReflectionProvider());
	}

	@Test
	public void usePatternMatchinForPrimitiveParameters() throws Exception {
		builder = newBuilder("/abc/{abc}/def/{def}/ghi/{ghi}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertTrue("valid uri", route.canHandle("/abc/AnythingHere/def/123/ghi/123.45"));
		assertTrue("valid uri", route.canHandle("/abc/AnythingHere/def/-123/ghi/-1"));
		assertTrue("valid uri with integer decimal", route.canHandle("/abc/AnythingHere/def/123/ghi/123"));
		assertFalse("invalid integer", route.canHandle("/abc/AnythingHere/def/Duh/ghi/123"));
		assertFalse("invalid decimal", route.canHandle("/abc/AnythingHere/def/123/ghi/kkk"));
	}

	private DefaultRouteBuilder newBuilder(String uri) {
		return new DefaultRouteBuilder(proxifier, typeFinder, converters, provider, 
				new JavaEvaluator(new DefaultReflectionProvider()), uri, encodingHandler);
	}

	@Test
	public void usePatternMatchingForRegexParameters() throws Exception {
		builder = newBuilder("/abc/{abc:a+b+c+}/def/{def}/ghi/{ghi}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/itIsInvalid/def/123/ghi/123.45"));
		assertTrue("valid uri", route.canHandle("/abc/aaabbccccc/def/-123/ghi/-1"));

	}

	@Test
	public void usingRegexesWithCurlyBraces() throws Exception {
		builder = newBuilder("/abc/{abc:[0-9A-Z]{5}}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/notValid"));
		assertFalse("invalid uri", route.canHandle("/abc/ABC123"));
		assertTrue("valid uri", route.canHandle("/abc/10AB3"));

	}
	@Test
	public void usingRegexesWithCurlyBracesNotOnTheEnd() throws Exception {
		builder = newBuilder("/abc/{abc:[0-9A-Z]{5}}/");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/notValid/"));
		assertFalse("invalid uri", route.canHandle("/abc/ABC123/"));
		assertTrue("valid uri", route.canHandle("/abc/10AB3/"));

	}

	@Test
	public void usingRegexesWithCurlyBracesNotOnTheEndAndOtherVar() throws Exception {
		builder = newBuilder("/abc/{abc:[0-9A-Z]{5}}/def/{def}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/notValid/def/12"));
		assertFalse("invalid uri", route.canHandle("/abc/ABC123/def/12"));
		assertTrue("valid uri", route.canHandle("/abc/10AB3/def/12"));

	}
	@Test
	public void usingRegexesWithCurlyBracesNotOnTheEndAndOtherVarAndManyOtherThings() throws Exception {
		builder = newBuilder("/abc/{abc:[0-9A-Z]{5}}{def}{xxx:[0-9A-Z]{5}}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/notValid/def/12"));
		assertFalse("invalid uri", route.canHandle("/abc/ABC123/def/12"));
		assertTrue("valid uri", route.canHandle("/abc/AAAAA14BBBBB"));

	}

	@Test
	public void usingRegexesWithAsterisksAtTheEnd() throws Exception {
		builder = newBuilder("/abc/{abc:[0-9A-Z]*}/def/{def}");

		builder.is(MyResource.class, method.getMethod());
		Route route = builder.build();

		assertFalse("invalid uri", route.canHandle("/abc/not_Valid/def/12"));
		assertTrue("valid uri", route.canHandle("/abc/ABC123/def/12"));
		assertTrue("valid uri", route.canHandle("/abc/10AB3/def/12"));

	}


	@Test
	public void fillingUriForPrimitiveParameters() throws Exception {
		builder = newBuilder("/abc/{abc}/def/{def}/ghi/{ghi}");

		Method method = MyResource.class.getDeclaredMethods()[0];
		builder.is(MyResource.class, method);
		Route route = builder.build();

		String url = route.urlFor(MyResource.class, method, "Anything", 123, new BigDecimal("123.45"));
		assertThat(url, is("/abc/Anything/def/123/ghi/123.45"));
	}

	static class Abc {
		private String def;

		public void setDef(String def) {
			this.def = def;
		}

		public String getDef() {
			return def;
		}
	}
	static class AbcResource {
		public void abc(Abc abc) {
		}
	}

	@Test
	public void shouldSupportPathsWithDotsAndAsterisks() throws SecurityException, NoSuchMethodException {
		builder = newBuilder("/my/{abc.def*}");

		Method method = AbcResource.class.getDeclaredMethods()[0];
		builder.is(AbcResource.class, method);
		
		Route route = builder.build();

		assertTrue(route.canHandle("/my/troublesome/uri"));
	}
	
	@Test
	@Ignore("Should it work someday?")
	public void shouldUseGenericParameters() throws SecurityException, NoSuchMethodException {
		builder = newBuilder("/my/{abc.y}");

		Method method = Generic.class.getDeclaredMethods()[0];
		builder.is(Specific.class, method);

		Route route = builder.build();

		assertTrue(route.canHandle("/my/123"));
		assertFalse(route.canHandle("/my/abc"));
	}

	static class Generic<T> {

		public void gee(T abc) {

		}
	}

	static class Specific extends Generic<X> {

	}
	static class X {
		private Integer y;

		public Integer getY() {
			return y;
		}
		public void setY(Integer y) {
			this.y = y;
		}
	}
}
