package nl.topicus.naming.kubernetes;

import java.util.Hashtable;

import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class KubeNamingStoreTest
{
	// Werkt alleen als je kube config hebt (maw dus niet op de buildserver)
	@Test
	@Ignore
	public void testSomething() throws NamingException
	{
		System.setProperty("org.jboss.logging.provider", "slf4j");
		KubeCtx ctx = new KubeCtx(new Hashtable<String, Object>()
		{
			{
				put(KubeNamingStore.NAMESPACE_PROPERTY, "acc-application");
			}
		});
		ctx.lookup("service.deployment.config");
		ctx.lookup("identity.federation/idp.id");
		ctx.lookup("somtoday.straat");
		Object port = ctx.lookup("entrustservice.port");
		Assert.assertTrue("" + port.getClass().toString(), Integer.class.isInstance(port));

		Object bugsnag = ctx.lookup("bugsnag.enabled");
		Assert.assertTrue(Boolean.class.isInstance(bugsnag));

		ctx.lookup("idp.certificate");
	}
}
