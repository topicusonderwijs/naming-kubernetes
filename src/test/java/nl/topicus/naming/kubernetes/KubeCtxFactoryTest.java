package nl.topicus.naming.kubernetes;

import static nl.topicus.naming.kubernetes.Utils.createConfigMap;
import static nl.topicus.naming.kubernetes.Utils.stub;

import java.security.Security;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;

public class KubeCtxFactoryTest {
  private static final int PORT = 8089;

  private ApiClient client;

	@Rule
  public WireMockRule wireMockRule = new WireMockRule(PORT);
  
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
        put(KubeCtx.APICLIENT_PROPERTY, client);
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