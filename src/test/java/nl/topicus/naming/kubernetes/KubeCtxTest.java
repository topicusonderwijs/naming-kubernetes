package nl.topicus.naming.kubernetes;

import static nl.topicus.naming.kubernetes.Utils.createConfigMap;
import static nl.topicus.naming.kubernetes.Utils.stub;

import java.security.Security;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CompositeName;
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

public class KubeCtxTest {
  private static final int PORT = 8089;

  private ApiClient client;

  private KubeCtx ctx;

	@Rule
  public WireMockRule wireMockRule = new WireMockRule(PORT);
  
  @BeforeClass
  public static void setup() 
  {
		Security.addProvider(new BouncyCastleProvider());
    System.setProperty("org.jboss.logging.provider", "slf4j");
  }

  @Before
  public void init() throws NamingException
  {
		client = new ApiClient();
		client.setBasePath("http://localhost:" + PORT);
    Configuration.setDefaultApiClient(client);
    
    ctx = new KubeCtx(new Hashtable<String, Object>()
    {
      private static final long serialVersionUID = 1L;
  
      {
        put(KubeCtx.APICLIENT_PROPERTY, client);
        put(KubeNamingStore.CONTEXT_PROPERTY, "");
        put(KubeNamingStore.NAMESPACE_PROPERTY, "kube-naming");
      }
    });
  }

  @Test
  public void testLookup() throws NamingException
  {
    stub(client, createConfigMap("root", Map.of("key", "value")));
    
    Assert.assertEquals("value", ctx.lookup("key"));
  }

  @Test
  public void testLookupByName() throws NamingException
  {
    stub(client, createConfigMap("root", Map.of("key", "value")));
    
    Assert.assertEquals("value", ctx.lookup(new CompositeName("key")));
  }

  @Test(expected = UnsupportedOperationException.class)
	public void testBindByName() throws NamingException
	{
		ctx.bind(new CompositeName(""), null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testBind() throws NamingException
	{
		ctx.bind("", null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRebindByName() throws NamingException
	{
		ctx.rebind(new CompositeName(""), null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRebind() throws NamingException
	{
		ctx.rebind("", null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnbindByName() throws NamingException
	{
	  ctx.unbind(new CompositeName(""));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnbind() throws NamingException
	{
		ctx.unbind("");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRenameByName() throws NamingException
	{
		ctx.rename(new CompositeName("old"), new CompositeName("new"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRename() throws NamingException
	{
		ctx.rebind("old", "new");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testListByName() throws NamingException
	{
		ctx.list(new CompositeName("list"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testList() throws NamingException
	{
		ctx.list("list");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testListBindingsByName() throws NamingException
	{
		ctx.listBindings(new CompositeName("binding"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testListBindings() throws NamingException
	{
		ctx.listBindings("binding");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDestroySubcontextByName() throws NamingException
	{
		ctx.destroySubcontext(new CompositeName("subcontext"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDestroySubcontext() throws NamingException
	{
		ctx.destroySubcontext("subcontext");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateSubcontextByName() throws NamingException
	{
		ctx.createSubcontext(new CompositeName("subcontext"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCreateSubcontext() throws NamingException
	{
		ctx.createSubcontext("subcontext");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLookupLinkByName() throws NamingException
	{
    ctx.lookupLink(new CompositeName("link"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLookupLink() throws NamingException
	{
		ctx.lookupLink("link");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetNameParserByName() throws NamingException
	{
		ctx.getNameParser(new CompositeName("parser"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetNameParser() throws NamingException
	{
		ctx.getNameParser("parser");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testComposeNameByName() throws NamingException
	{
		ctx.composeName(new CompositeName("name"), new CompositeName("prefix"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testComposeName() throws NamingException
	{
		ctx.composeName("name", "prefix");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddToEnvironment() throws NamingException
	{
		ctx.addToEnvironment("prop", "val");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemoveFromEnvironment() throws NamingException
	{
		ctx.removeFromEnvironment("prop");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetEnvironment() throws NamingException
	{
		ctx.getEnvironment();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testClose() throws NamingException
	{
		ctx.close();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetNameInNamespace() throws NamingException
	{
		ctx.getNameInNamespace();
	}
}