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

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;

public class Utils
{
	public static V1ConfigMap createConfigMap(String name, Map<String, String> data)
	{
		return createConfigMap(name, Collections.emptyMap(), data);
	}

	public static V1ConfigMap createConfigMap(String name, Map<String, String> annotations,
			Map<String, String> data)
	{
		return new V1ConfigMap().apiVersion("v1")
			.kind("ConfigMap")
			.metadata(new V1ObjectMeta().name(name)
				.labels(Map.of(KubeNamingStore.LABEL_SELECTOR, ""))
				.annotations(annotations))
			.data(data);
	}

	public static V1Secret createSecret(String name, String type, Map<String, byte[]> data)
	{
		return createSecret(name, type, Collections.emptyMap(), data);
	}

	public static V1Secret createSecret(String name, String type, Map<String, String> annotations,
			Map<String, byte[]> data)
	{
		return new V1Secret().apiVersion("v1")
			.kind("Secret")
			.metadata(new V1ObjectMeta().name(name)
				.labels(Map.of(KubeNamingStore.LABEL_SELECTOR, ""))
				.annotations(annotations))
			.type(type)
			.data(data);
	}

	public static void stub(ApiClient client, V1ConfigMap configMap)
	{
		stub(client, new V1ConfigMapList().items(Collections.singletonList(configMap)));
	}

	public static void stub(ApiClient client, V1ConfigMapList configMapList)
	{
		stubFor(get(urlPathEqualTo("/api/v1/namespaces/kube-naming/configmaps"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(client.getJSON().serialize(configMapList))));
	}

	public static void stub(ApiClient client, V1Secret... secrets)
	{
		stub(client, new V1SecretList().items(Arrays.asList(secrets)));
	}

	public static void stub(ApiClient client, V1SecretList secretList)
	{
		stubFor(get(urlPathEqualTo("/api/v1/namespaces/kube-naming/secrets"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(client.getJSON().serialize(secretList))));
	}
}
