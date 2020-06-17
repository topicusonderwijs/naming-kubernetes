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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.naming.Name;
import javax.naming.NamingException;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jboss.logging.Logger;

public class KubeNamingStore
{
	public static final String CONTEXT_PROPERTY = "java.naming.kubernetes.context";

	public static final String NAMESPACE_PROPERTY = "java.naming.kubernetes.namespace";

	private static final Logger logger = Logger.getLogger(KubeNamingStore.class);

	static final String LABEL_SELECTOR = "k8s.naming.topicus.nl/externalcontext";

	static final String ANNOTATION_KEY = "k8s.naming.topicus.nl/context";

	static final String ANNOTATION_CERTIFICATE_KEY = "k8s.naming.topicus.nl/key_certificate";

	static final String ANNOTATION_CERTIFICATE_STRING = "k8s.naming.topicus.nl/certificate_string";

	static final String ANNOTATION_CERTIFICATE_HEADER_FOOTER =
		"k8s.naming.topicus.nl/certificate_remove_header_footer";

	static final String ANNOTATION_PRIVATEKEY_KEY = "k8s.naming.topicus.nl/key_privatekey";

	static final String ANNOTATION_PRIVATEKEY_STRING = "k8s.naming.topicus.nl/privatekey_string";

	static final String ANNOTATION_PRIVATEKEY_FORMAT = "k8s.naming.topicus.nl/privatekey_format";

	static final String SECRET_TLS = "kubernetes.io/tls";

	static final String PEM_CERTIFICATE_START = "-----BEGIN CERTIFICATE-----";

	static final String PEM_CERTIFICATE_END = "-----END CERTIFICATE-----";

	private final CoreV1Api corev1;

	private final LoadingCache<Name, Optional<Object>> values;

	private final String context;

	private final String namespace;

	public KubeNamingStore(final Hashtable<String, Object> envprops)
	{
		context = (String) envprops.getOrDefault(CONTEXT_PROPERTY, "");
		namespace = (String) envprops.getOrDefault(NAMESPACE_PROPERTY, "default");
		corev1 = new CoreV1Api();
		values = createCache();
	}

	private LoadingCache<Name, Optional<Object>> createCache()
	{
		return CacheBuilder.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS)
			.refreshAfterWrite(15, TimeUnit.MINUTES)
			.build(new CacheLoader<Name, Optional<Object>>()
			{
				@Override
				public Optional<Object> load(Name name) throws Exception
				{
					try
					{
						return Optional.ofNullable(loadInternal(name));
					}
					catch (ApiException e)
					{
						logger.errorv(e, "Failed to call Kubernetes API for ''{0}''!",
							name.toString());
						return Optional.empty();
					}
				}
			});
		// TODO : async refresh / k8s watch
	}

	private Object loadInternal(final Name name) throws ApiException
	{
		logger.debugv("Lookup for ''{0}'' in Kubernetes naming context...", name.toString());
		String context = getContext(name);
		String key = getKeyWithoutContext(name);
		Object result = loadFromConfigMap(context, key);
		return result == null ? loadFromSecret(context, key) : result;
	}

	private Object loadFromConfigMap(final String context, final String key) throws ApiException
	{
		V1ConfigMapList configMaps = corev1.listNamespacedConfigMap(namespace, null, null, null,
			null, getLabelSelector(), null, null, null, null);
		for (V1ConfigMap configMap : configMaps.getItems())
		{
			String ann = null;
			if (configMap.getMetadata().getAnnotations() != null)
			{
				ann = configMap.getMetadata().getAnnotations().get(ANNOTATION_KEY);
			}

			if ((context == null && ann != null) || (context != null && ann == null)
				|| (context != null && ann != null && !context.equalsIgnoreCase(ann)))
			{
				logger.debugv(
					"Skipping configmap ''{3}'' (annotation {2}) for key ''{1}'' (context ''{0}'')",
					context, key, ann, configMap.getMetadata().getName());
				continue;
			}

			String value = configMap.getData().get(key);
			if (value != null)
			{
				logger.debugv(
					"Found value ''{0}'' for key ''{1}'' (context ''{2}'') in configmap ''{3}''",
					value, key, context != null ? context : "/", configMap.getMetadata().getName());
				return convertToJavaType(value);
			}
		}
		logger.debugv("Key ''{0}'' (context {1}) not found in configmap", key, context);
		return null;
	}

	private Object convertToJavaType(String value)
	{
		if (Boolean.TRUE.toString().equalsIgnoreCase(value))
		{
			return Boolean.TRUE;
		}
		if (Boolean.FALSE.toString().equalsIgnoreCase(value))
		{
			return Boolean.FALSE;
		}

		// FIXME: no negative numbers and everything is an int
		if (StringUtils.isNumeric(value))
		{
			try
			{
				return Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				// ignore
			}
		}

		return value;
	}

	private Object loadFromSecret(final String context, final String key) throws ApiException
	{
		V1SecretList secrets = corev1.listNamespacedSecret(namespace, null, null, null, null,
			getLabelSelector(), null, null, null, null);
		for (V1Secret secret : secrets.getItems())
		{
			String ann = null;
			if (secret.getMetadata().getAnnotations() != null)
			{
				ann = secret.getMetadata().getAnnotations().get(ANNOTATION_KEY);
			}

			if ((context == null && ann != null) || (context != null && ann == null)
				|| (context != null && ann != null && !context.equalsIgnoreCase(ann)))
			{
				logger.debugv(
					"Skipping secret ''{3}'' (annotation {2}) for key ''{1}'' (context ''{0}'')",
					context, key, ann, secret.getMetadata().getName());
				continue;
			}

			if (SECRET_TLS.equalsIgnoreCase(secret.getType()))
			{
				Object result = loadFromTlsSecret(secret, key);
				if (result != null)
				{
					logger.debugv("Found key ''{0}'' (context ''{1}'') in tls secret ''{2}''", key,
						context != null ? context : "/", secret.getMetadata().getName());
					return result;
				}
				continue;
			}

			byte[] value = secret.getData().get(key);
			if (value != null)
			{
				logger.debugv("Found key ''{0}'' (context ''{1}'') in secret ''{2}''", key,
					context != null ? context : "/", secret.getMetadata().getName());
				return new String(value, Charsets.UTF_8);
			}
		}
		logger.debugv("Key ''{0}'' (context {1}) not found in any secret", key, context);
		return null;
	}

	private Object loadFromTlsSecret(final V1Secret secret, final String key)
	{
		String annCertKey = secret.getMetadata().getAnnotations().get(ANNOTATION_CERTIFICATE_KEY);
		if (annCertKey == null)
		{
			logger.debugv(
				"Skipping tls secret ''{2}'' (missing certificate annotation) for key ''{1}'' (context ''{0}'')",
				context, key, secret.getMetadata().getName());
			return null;
		}

		String annPrivKey = secret.getMetadata().getAnnotations().get(ANNOTATION_PRIVATEKEY_KEY);
		if (annPrivKey == null)
		{
			logger.debugv(
				"Skipping tls secret ''{2}'' (missing privatekey annotation) for key ''{1}'' (context ''{0}'')",
				context, key, secret.getMetadata().getName());
			return null;
		}

		if (annCertKey.equals(key))
		{
			boolean certAsString = Boolean.parseBoolean(
				secret.getMetadata().getAnnotations().get(ANNOTATION_CERTIFICATE_STRING));
			if (!certAsString)
			{
				return secret.getData().get("tls.crt");
			}

			String crt = new String(secret.getData().get("tls.crt"), Charsets.UTF_8);
			if (Boolean.parseBoolean(
				secret.getMetadata().getAnnotations().get(ANNOTATION_CERTIFICATE_HEADER_FOOTER)))
			{
				crt = removePEMHeaderAndFooter(crt, PEM_CERTIFICATE_START, PEM_CERTIFICATE_END);
			}
			return crt;
		}
		else if (annPrivKey.equals(key))
		{
			boolean pkAsString = Boolean.parseBoolean(
				secret.getMetadata().getAnnotations().get(ANNOTATION_PRIVATEKEY_STRING));

			byte[] pk = secret.getData().get("tls.key");
			if ("PKCS#8".equalsIgnoreCase(
				secret.getMetadata().getAnnotations().get(ANNOTATION_PRIVATEKEY_FORMAT)))
			{
				pk = convertToPKCS8(pk);
			}

			return pk != null && pkAsString ? new String(pk) : pk;
		}
		return null;
	}

	private String removePEMHeaderAndFooter(final String data, final String header,
			final String footer)
	{
		String result = StringUtils.remove(data, "\n");
		result = StringUtils.removeStart(result, header);
		result = StringUtils.removeEnd(result, footer);
		return result;
	}

	private byte[] convertToPKCS8(final byte[] pkdata)
	{
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		try (PEMParser reader =
			new PEMParser(new InputStreamReader(new ByteArrayInputStream(pkdata))))
		{
			KeyPair kp = converter.getKeyPair((PEMKeyPair) reader.readObject());
			return Base64.getEncoder().encode(kp.getPrivate().getEncoded());
		}
		catch (IOException e)
		{
			logger.errorv("Failed to decode private key. Reason: {0}", e.getMessage());
			return null;
		}
	}

	private String getContext(final Name name)
	{
		String fn = name.toString();
		int i = fn.lastIndexOf('/');
		return i > 0 ? fn.substring(0, i) : null;
	}

	private String getKeyWithoutContext(final Name name)
	{
		String fn = name.toString();
		int i = fn.lastIndexOf('/');
		return i > 0 ? fn.substring(i + 1) : fn;
	}

	private String getLabelSelector()
	{
		return String.format("%s in (, %s)", LABEL_SELECTOR, context);
	}

	public Object get(final Name key) throws NamingException
	{
		try
		{
			Optional<Object> result = values.get(key);
			if (result.isPresent())
			{
				return result.get();
			}
			throw new NamingException("Key " + key.toString() + " not found!");
		}
		catch (ExecutionException | UncheckedExecutionException e)
		{
			NamingException ne =
				new NamingException("Failed to load value for key '" + key.toString() + "'");
			ne.setRootCause(e);
			throw ne;
		}
	}
}
