package nl.topicus.naming.etcd;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import com.google.common.base.Splitter;
import org.jboss.logging.Logger;

public class EtcdCtxFactory implements InitialContextFactory
{
	private static final Logger logger = Logger.getLogger(EtcdCtxFactory.class);

	protected static volatile Context initialContext = null;

	@Override
	public synchronized Context getInitialContext(Hashtable< ? , ? > envprops)
			throws NamingException
	{
		if (initialContext == null)
		{
			String providerUrl = (envprops != null && envprops.containsKey(Context.PROVIDER_URL))
				? (String) envprops.get(Context.PROVIDER_URL) : "http://127.0.0.1:2379";

			logger.infov("Initialising etcd context for ''{0}''...", providerUrl);
			try
			{
				initialContext = new EtcdCtx(Splitter.on(",").splitToList(providerUrl), envprops);
			}
			catch (Throwable e)
			{
				// WildFly silently ignores exceptions and repeatable calls getInitialContext
				logger.error("Failed to initialise etcd context!", e);
				throw e;
			}
		}
		return initialContext;
	}
}
