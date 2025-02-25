/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.jersey.client;

import io.bootique.di.Injector;
import io.bootique.jersey.client.HttpClientFactory;
import io.bootique.jersey.client.HttpClientFactoryFactory;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class HttpClientFactoryFactoryTest {

	private Injector mockInjector = mock(Injector.class);

	@Test
	public void testCreateClientFactory() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setAsyncThreadPoolSize(5);
		factoryFactory.setConnectTimeoutMs(101);
		factoryFactory.setFollowRedirects(true);
		factoryFactory.setReadTimeoutMs(203);

		HttpClientFactory factory = factoryFactory.createClientFactory(mockInjector, Collections.emptySet());
		assertNotNull(factory);

		Client client = factory.newClient();

		try {

			assertEquals(5, client.getConfiguration().getProperty(ClientProperties.ASYNC_THREADPOOL_SIZE));
			assertEquals(101, client.getConfiguration().getProperty(ClientProperties.CONNECT_TIMEOUT));
			assertEquals(true, client.getConfiguration().getProperty(ClientProperties.FOLLOW_REDIRECTS));
			assertEquals(203, client.getConfiguration().getProperty(ClientProperties.READ_TIMEOUT));

		} finally {
			client.close();
		}
	}
}
