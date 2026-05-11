/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package nl.topicus.naming.kubernetes;

import static nl.topicus.naming.kubernetes.Utils.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import java.security.Security;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.NamingException;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1SecretList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class KubeCtxTest
{
	private static final int PORT = 8089;

	private ApiClient client;

	private KubeCtx ctx;

	@RegisterExtension
	static WireMockExtension wireMockRule = WireMockExtension.newInstance()
		.options(wireMockConfig().port(PORT))
		.configureStaticDsl(true)
		.build();

	@BeforeAll
	public static void setup()
	{
		Security.addProvider(new BouncyCastleProvider());
		System.setProperty("org.jboss.logging.provider", "slf4j");
		System.setProperty("org.junit.test", "true");
	}

	@BeforeEach
	public void init() throws NamingException
	{
		client = new ApiClient();
		client.setBasePath("http://localhost:" + PORT);
		Configuration.setDefaultApiClient(client);

		ctx = new KubeCtx(new Hashtable<String, Object>()
		{
			private static final long serialVersionUID = 1L;

			{
				put(KubeNamingStore.CONTEXT_PROPERTY, "");
				put(KubeNamingStore.NAMESPACE_PROPERTY, "kube-naming");
			}
		});
	}

	@Test
	public void testLookup() throws NamingException
	{
		stub(client, createConfigMap("root", Map.of("key", "value")));

		assertEquals("value", ctx.lookup("key"));
	}

	@Test
	public void testLookupByName() throws NamingException
	{
		stub(client, createConfigMap("root", Map.of("key", "value")));

		assertEquals("value", ctx.lookup(new CompositeName("key")));
	}

	@Test
	public void testMissingLookupByName()
	{
		stub(client, createConfigMap("root", Map.of()));
		stub(client, new V1SecretList());

		assertThrows(NamingException.class, () -> ctx.lookup(new CompositeName("key")));
	}

	@Test
	public void testBindByName()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.bind(new CompositeName(""), null));
	}

	@Test
	public void testBind()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.bind("", null));
	}

	@Test
	public void testRebindByName()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.rebind(new CompositeName(""), null));
	}

	@Test
	public void testRebind()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.rebind("", null));
	}

	@Test
	public void testUnbindByName()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.unbind(new CompositeName("")));
	}

	@Test
	public void testUnbind()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.unbind(""));
	}

	@Test
	public void testRenameByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.rename(new CompositeName("old"), new CompositeName("new")));
	}

	@Test
	public void testRename()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.rename("old", "new"));
	}

	@Test
	public void testListByName()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.list(new CompositeName("list")));
	}

	@Test
	public void testList()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.list("list"));
	}

	@Test
	public void testListBindingsByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.listBindings(new CompositeName("binding")));
	}

	@Test
	public void testListBindings()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.listBindings("binding"));
	}

	@Test
	public void testDestroySubcontextByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.destroySubcontext(new CompositeName("subcontext")));
	}

	@Test
	public void testDestroySubcontext()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.destroySubcontext("subcontext"));
	}

	@Test
	public void testCreateSubcontextByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.createSubcontext(new CompositeName("subcontext")));
	}

	@Test
	public void testCreateSubcontext()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.createSubcontext("subcontext"));
	}

	@Test
	public void testLookupLinkByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.lookupLink(new CompositeName("link")));
	}

	@Test
	public void testLookupLink()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.lookupLink("link"));
	}

	@Test
	public void testGetNameParserByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.getNameParser(new CompositeName("parser")));
	}

	@Test
	public void testGetNameParser()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.getNameParser("parser"));
	}

	@Test
	public void testComposeNameByName()
	{
		assertThrows(UnsupportedOperationException.class,
			() -> ctx.composeName(new CompositeName("name"), new CompositeName("prefix")));
	}

	@Test
	public void testComposeName()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.composeName("name", "prefix"));
	}

	@Test
	public void testAddToEnvironment()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.addToEnvironment("prop", "val"));
	}

	@Test
	public void testRemoveFromEnvironment()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.removeFromEnvironment("prop"));
	}

	@Test
	public void testGetEnvironment()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.getEnvironment());
	}

	@Test
	public void testClose()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.close());
	}

	@Test
	public void testGetNameInNamespace()
	{
		assertThrows(UnsupportedOperationException.class, () -> ctx.getNameInNamespace());
	}
}
