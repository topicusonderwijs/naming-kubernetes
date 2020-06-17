/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.topicus.naming.kubernetes;

import static nl.topicus.naming.kubernetes.Utils.*;

import java.security.Security;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class KubeCtxFactoryTest
{
	private static final int PORT = 8089;

	private ApiClient client;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);

	@BeforeClass
	public static void setup()
	{
		Security.addProvider(new BouncyCastleProvider());
		System.setProperty("org.jboss.logging.provider", "slf4j");
		System.setProperty("org.junit.test", "true");
	}

	@Before
	public void init()
	{
		client = new ApiClient();
		client.setBasePath("http://localhost:" + PORT);
		Configuration.setDefaultApiClient(client);
	}

	@Test
	public void testInitialContext() throws NamingException
	{
		stub(client, createConfigMap("root", Map.of("key", "value")));

		KubeCtxFactory factory = new KubeCtxFactory();
		Context ctx1 = factory.getInitialContext(new Hashtable<String, Object>()
		{
			private static final long serialVersionUID = 1L;

			{
				put(KubeNamingStore.CONTEXT_PROPERTY, "");
				put(KubeNamingStore.NAMESPACE_PROPERTY, "kube-naming");
			}
		});
		Assert.assertNotNull(ctx1);

		Context ctx2 = factory.getInitialContext(new Hashtable<>());
		Assert.assertEquals(ctx1, ctx2);

		Assert.assertEquals("value", ctx1.lookup("key"));
	}
}
