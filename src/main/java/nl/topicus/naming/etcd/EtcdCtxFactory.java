package nl.topicus.naming.etcd;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import com.google.common.base.Splitter;

public class EtcdCtxFactory implements InitialContextFactory
{
	@Override
	public Context getInitialContext(Hashtable< ? , ? > envprops) throws NamingException
	{
		String providerUrl = (envprops != null && envprops.containsKey(Context.PROVIDER_URL))
			? (String) envprops.get(Context.PROVIDER_URL) : "http://127.0.0.1:2379";

		return new EtcdCtx(Splitter.on(",").splitToList(providerUrl), envprops);
	}
}
