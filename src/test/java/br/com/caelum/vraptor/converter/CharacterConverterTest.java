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

package br.com.caelum.vraptor.converter;

import static br.com.caelum.vraptor.VRaptorMatchers.hasConversionException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import br.com.caelum.vraptor.converter.CharacterConverter;

public class CharacterConverterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private CharacterConverter converter;

	@Before
	public void setup() {
		this.converter = new CharacterConverter();
	}

	@Test
	public void shouldBeAbleToConvertCharacters() {
		assertThat(converter.convert("Z", Character.class), is(equalTo('Z')));
	}

	@Test
	public void shouldComplainAboutStringTooBig() {
		exception.expect(hasConversionException("--- is not a valid character."));
		converter.convert("---", Character.class);
	}

	@Test
	public void shouldNotComplainAboutNullAndEmpty() {
		assertThat(converter.convert(null, Character.class), is(nullValue()));
		assertThat(converter.convert("", Character.class), is(nullValue()));
	}
}
