package nl.topicus.naming.kubernetes;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Ignore;
import org.junit.Test;

public class KubeNamingStoreTest
{
	// Werkt alleen als je kube config hebt (maw dus niet op de buildserver)
	@Test
	@Ignore
	public void testSomething() throws NamingException
	{
		Security.addProvider(new BouncyCastleProvider());
		System.setProperty("org.jboss.logging.provider", "slf4j");
		Context ctx = new KubeCtxFactory().getInitialContext(new Hashtable<String, Object>()
		{
			{
				put(KubeNamingStore.CONTEXT_PROPERTY, "");
				put(KubeNamingStore.NAMESPACE_PROPERTY, "som-build");
			}
		});
		// ctx.lookup("service.deployment.config");
		// ctx.lookup("identity.federation/idp.id");
		// ctx.lookup("somtoday.straat");
		// Object port = ctx.lookup("entrustservice.port");
		// Assert.assertTrue("" + port.getClass().toString(), Integer.class.isInstance(port));
		//
		// Object bugsnag = ctx.lookup("bugsnag.enabled");
		// Assert.assertTrue(Boolean.class.isInstance(bugsnag));

		String crt = (String) ctx.lookup("identity.federation/idp.certificate"); // shared
		System.out.println(crt);

		String key = (String) ctx.lookup("identity.federation/idp.privatekey"); // authenticator
																				// specifiek
		System.out.println(key);

		// key = PKCS#1 format, cobra verwacht PKCS#8 format

		try
		{
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPrivateKey privKey = (RSAPrivateKey) factory
				.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new RuntimeException(e);
		}
	}
}
