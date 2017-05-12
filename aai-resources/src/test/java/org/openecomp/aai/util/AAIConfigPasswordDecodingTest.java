/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.util;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openecomp.aai.exceptions.AAIException;

public class AAIConfigPasswordDecodingTest {

	/**
	 * Configure.
	 *
	 * @throws AAIException the AAI exception
	 */
	@BeforeClass
	public static void configure() throws AAIException {
		System.setProperty("AJSC_HOME", "./src/test/resources/");
		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");; //fake prop file for testing
	}
	
	/**
	 * Password check.
	 *
	 * @throws AAIException the AAI exception
	 */
	@Test
	public void passwordCheck() throws AAIException {
		assertEquals("password", "aaiuser123", AAIConfig.get("ecm.auth.password"));
	}

}
