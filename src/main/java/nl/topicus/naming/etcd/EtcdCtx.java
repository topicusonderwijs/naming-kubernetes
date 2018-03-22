package nl.topicus.naming.etcd;

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import javax.naming.*;
import javax.net.ssl.SSLException;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.ClientBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import org.jboss.logging.Logger;

public class EtcdCtx implements Context, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String CA_CERT = "java.naming.etcd.cacert";

	private static final Logger logger = Logger.getLogger(EtcdCtx.class);

	private final List<String> endpoints;

	private final Hashtable<String, Object> envprops;

	private final EtcdNamingStore store;

	@SuppressWarnings("unchecked")
	public EtcdCtx(final List<String> endpoints, final Hashtable< ? , ? > props)
			throws NamingException
	{
		this.endpoints = endpoints;
		envprops = props != null ? (Hashtable<String, Object>) props.clone() : null;

		try
		{
			this.store = new EtcdNamingStore(createEtcdClient(), envprops);
		}
		catch (Exception e)
		{
			NamingException ne = new NamingException("Failed to create etcd store!");
			ne.setRootCause(e);
			throw ne;
		}
	}

	private Client createEtcdClient() throws SSLException
	{
		ClientBuilder clientBuilder = Client.builder().endpoints(endpoints);

		if (endpoints.get(0).startsWith("https"))
		{
			clientBuilder.sslContext(createSslContext());
		}

		return clientBuilder.build();
	}

	private SslContext createSslContext() throws SSLException
	{
		if (!envprops.containsKey(CA_CERT))
		{
			throw new SSLException(
				"Missing required environment parameter '" + CA_CERT + "' for tls!");
		}

		SslContext sslContext = null;
		sslContext = GrpcSslContexts.forClient()
			.protocols("TLSv1.2")
			// TODO: .keyManager(keyCertChainFile, keyFile) for client auth
			.trustManager(new File((String) envprops.get(CA_CERT)))
			.build();
		return sslContext;
	}

	@Override
	public Object lookup(Name name) throws NamingException
	{
		logger.debugv("Lookup for ''{0}''...", name.toString());
		Object value = store.get(name);
		logger.debugv("Lookup for ''{0}'' returned {1} result.", name.toString(),
			value != null ? "a" : "no");
		return value;
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		return lookup(new CompositeName(name));
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void bind(String name, Object obj) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void unbind(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void rename(Name oldName, Name newName) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void rename(String oldName, String newName) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void destroySubcontext(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Context createSubcontext(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object lookupLink(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object lookupLink(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Hashtable< ? , ? > getEnvironment() throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws NamingException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNameInNamespace() throws NamingException
	{
		throw new UnsupportedOperationException();
	}
}
