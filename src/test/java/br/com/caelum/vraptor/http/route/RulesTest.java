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

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.vraptor.core.DefaultReflectionProvider;
import br.com.caelum.vraptor.http.EncodingHandler;
import br.com.caelum.vraptor.http.route.DefaultRouteBuilder;
import br.com.caelum.vraptor.http.route.IllegalRouteException;
import br.com.caelum.vraptor.http.route.JavaEvaluator;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.http.route.Rules;
import br.com.caelum.vraptor.proxy.Proxifier;

public class RulesTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private @Mock Router router;
	private @Mock Proxifier proxifier;
	private DefaultRouteBuilder routeBuilder;
	private @Mock EncodingHandler encodingHandler;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		routeBuilder = new DefaultRouteBuilder(proxifier, null, null, null, 
				new JavaEvaluator(new DefaultReflectionProvider()), "",encodingHandler);
		when(router.builderFor("")).thenReturn(routeBuilder);
	}

	@Test
	public void allowsAdditionOfRouteBuildersByDefaultWithNoStrategy() {
		exception.expect(IllegalRouteException.class);

		new Rules(router) {
			@Override
			public void routes() {
				routeFor("");
			}
		};
	}
}
