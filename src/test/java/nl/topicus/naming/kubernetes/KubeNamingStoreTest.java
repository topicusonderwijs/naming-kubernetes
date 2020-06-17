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

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.NamingException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class KubeNamingStoreTest
{
	private static final int PORT = 8089;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);

	private ApiClient client;

	private KubeNamingStore store;

	@BeforeClass
	public static void setup()
	{
		Security.addProvider(new BouncyCastleProvider());
		System.setProperty("org.jboss.logging.provider", "slf4j");
	}

	@Before
	public void init()
	{
		client = new ApiClient();
		client.setBasePath("http://localhost:" + PORT);
		Configuration.setDefaultApiClient(client);

		store = new KubeNamingStore(new Hashtable<String, Object>()
		{
			private static final long serialVersionUID = 1L;

			{
				put(KubeNamingStore.CONTEXT_PROPERTY, "");
				put(KubeNamingStore.NAMESPACE_PROPERTY, "kube-naming");
			}
		});
	}

	@Test
	public void testJavaTypeConversions() throws NamingException
	{
		stub(client, createConfigMap("conversions",
			Map.of("key1", "value1", "key2", "42", "key3", "True", "key4", "false")));

		Object value = store.get(new CompositeName("key1"));
		Assert.assertEquals(String.class, value.getClass());
		Assert.assertEquals("value1", value);

		value = store.get(new CompositeName("key2"));
		Assert.assertEquals(Integer.class, value.getClass());
		Assert.assertEquals(42, value);

		value = store.get(new CompositeName("key3"));
		Assert.assertEquals(Boolean.class, value.getClass());
		Assert.assertEquals(true, value);

		value = store.get(new CompositeName("key4"));
		Assert.assertEquals(Boolean.class, value.getClass());
		Assert.assertEquals(false, value);
	}

	@Test(expected = NamingException.class)
	public void testMissingKey() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client, new V1SecretList());

		store.get(new CompositeName("key"));
	}

	private V1ConfigMapList createSubcontextsConfigMapList()
	{
		V1ConfigMap root = createConfigMap("root", Map.of("key1", "value1"));
		V1ConfigMap sub1 = createConfigMap("sub1", Map.of(KubeNamingStore.ANNOTATION_KEY, "sub1"),
			Map.of("key2", "value2"));
		V1ConfigMap sub2 = createConfigMap("sub2", Map.of(KubeNamingStore.ANNOTATION_KEY, "sub2"),
			Map.of("key3", "value3"));

		return new V1ConfigMapList().items(Arrays.asList(root, sub1, sub2));
	}

	@Test
	public void testSubContextsWithConfigMap() throws NamingException
	{
		stub(client, createSubcontextsConfigMapList());

		Object value = store.get(new CompositeName("key1"));
		Assert.assertEquals("value1", value);

		value = store.get(new CompositeName("sub1/key2"));
		Assert.assertEquals("value2", value);

		value = store.get(new CompositeName("sub2/key3"));
		Assert.assertEquals("value3", value);
	}

	@Test(expected = NamingException.class)
	public void testMissingKeyInSubContextWithConfigMap() throws NamingException
	{
		stub(client, createSubcontextsConfigMapList());
		stub(client, new V1SecretList());

		store.get(new CompositeName("sub1/key3"));
	}

	@Test
	public void testKeyFromSecret() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client, createSecret("my-secret", "Opaque",
			Map.of("key1", "secret1".getBytes(Charsets.UTF_8))));

		Object value = store.get(new CompositeName("key1"));
		Assert.assertEquals("secret1", value);
	}

	private V1SecretList createSubcontextsSecretList()
	{
		V1Secret root =
			createSecret("root", "Opaque", Map.of("key1", "secret1".getBytes(Charsets.UTF_8)));
		V1Secret sub1 =
			createSecret("sub1", "Opaque", Map.of(KubeNamingStore.ANNOTATION_KEY, "sub1"),
				Map.of("key2", "secret2".getBytes(Charsets.UTF_8)));
		V1Secret sub2 =
			createSecret("sub2", "Opaque", Map.of(KubeNamingStore.ANNOTATION_KEY, "sub2"),
				Map.of("key3", "secret3".getBytes(Charsets.UTF_8)));

		return new V1SecretList().items(Arrays.asList(root, sub1, sub2));
	}

	@Test
	public void testSubContextsWithSecret() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client, createSubcontextsSecretList());

		Object value = store.get(new CompositeName("key1"));
		Assert.assertEquals("secret1", value);

		value = store.get(new CompositeName("sub1/key2"));
		Assert.assertEquals("secret2", value);

		value = store.get(new CompositeName("sub2/key3"));
		Assert.assertEquals("secret3", value);
	}

	@Test
	public void testTlsSecret() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client,
			createSecret("my-crt-secret-1", KubeNamingStore.SECRET_TLS,
				Map.of(KubeNamingStore.ANNOTATION_CERTIFICATE_KEY, "crt1",
					KubeNamingStore.ANNOTATION_PRIVATEKEY_KEY, "pkey1",
					KubeNamingStore.ANNOTATION_PRIVATEKEY_STRING, "true"),
				Map.of("tls.crt", "certificate1".getBytes(Charsets.UTF_8), "tls.key",
					"privatekey1".getBytes(Charsets.UTF_8))),
			createSecret("my-crt-secret-2", KubeNamingStore.SECRET_TLS,
				Map.of(KubeNamingStore.ANNOTATION_CERTIFICATE_KEY, "crt2",
					KubeNamingStore.ANNOTATION_PRIVATEKEY_KEY, "pkey2",
					KubeNamingStore.ANNOTATION_CERTIFICATE_STRING, "true"),
				Map.of("tls.crt", "certificate2".getBytes(Charsets.UTF_8), "tls.key",
					"privatekey2".getBytes(Charsets.UTF_8))));

		byte[] crt1 = (byte[]) store.get(new CompositeName("crt1"));
		Assert.assertArrayEquals("certificate1".getBytes(Charsets.UTF_8), crt1);

		Object pkey1 = store.get(new CompositeName("pkey1"));
		Assert.assertEquals("privatekey1", pkey1);

		Object crt2 = store.get(new CompositeName("crt2"));
		Assert.assertEquals("certificate2", crt2);

		byte[] pkey2 = (byte[]) store.get(new CompositeName("pkey2"));
		Assert.assertArrayEquals("privatekey2".getBytes(Charsets.UTF_8), pkey2);
	}

	@Test(expected = NamingException.class)
	public void testMissingCertificateKeyAnnotation() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client, createSecret("my-secret", KubeNamingStore.SECRET_TLS,
			Map.of("tls.crt", "certificate".getBytes(Charsets.UTF_8))));

		store.get(new CompositeName("no-valid-secret"));
	}

	@Test(expected = NamingException.class)
	public void testMissingPrivateKeyKeyAnnotation() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client,
			createSecret("my-secret", KubeNamingStore.SECRET_TLS,
				Map.of(KubeNamingStore.ANNOTATION_CERTIFICATE_KEY, "crt1"),
				Map.of("tls.key", "privatekey".getBytes(Charsets.UTF_8))));

		store.get(new CompositeName("no-valid-secret"));
	}

	@Test
	public void testPEMHeaderAndFooterRemoval() throws NamingException
	{
		stub(client, new V1ConfigMapList());
		stub(client,
			createSecret("my-secret", KubeNamingStore.SECRET_TLS,
				Map.of(KubeNamingStore.ANNOTATION_CERTIFICATE_KEY, "crt",
					KubeNamingStore.ANNOTATION_PRIVATEKEY_KEY, "pkey",
					KubeNamingStore.ANNOTATION_CERTIFICATE_STRING, "true",
					KubeNamingStore.ANNOTATION_CERTIFICATE_HEADER_FOOTER, "true"),
				Map.of("tls.crt",
					String
						.format("%sprivatekeydata%s", KubeNamingStore.PEM_CERTIFICATE_START,
							KubeNamingStore.PEM_CERTIFICATE_END)
						.getBytes(Charsets.UTF_8))));

		Object value = store.get(new CompositeName("crt"));
		Assert.assertEquals(String.class, value.getClass());
		Assert.assertEquals("privatekeydata", value);
	}

	@Test
	public void testPKCS8Conversion() throws IOException, NamingException
	{
		byte[] pkey =
			ByteStreams.toByteArray(KubeNamingStoreTest.class.getResourceAsStream("dummy.key"));
		PrivateKeyInfo pkcs8Key = (PrivateKeyInfo) new PEMParser(
			new InputStreamReader(KubeNamingStoreTest.class.getResourceAsStream("dummy.pem")))
				.readObject();

		stub(client, new V1ConfigMapList());
		stub(client, createSecret("my-secret", KubeNamingStore.SECRET_TLS,
			Map.of(KubeNamingStore.ANNOTATION_CERTIFICATE_KEY, "crt",
				KubeNamingStore.ANNOTATION_PRIVATEKEY_KEY, "pkey",
				KubeNamingStore.ANNOTATION_PRIVATEKEY_FORMAT, "PKCS#8"),
			Map.of("tls.key", pkey)));

		byte[] value = (byte[]) store.get(new CompositeName("pkey"));
		// debug: Assert.assertEquals(new String(Base64.getEncoder().encode(pkcs8Key.getEncoded())),
		// new String(value));
		Assert.assertArrayEquals(Base64.getEncoder().encode(pkcs8Key.getEncoded()), value);
	}
}
