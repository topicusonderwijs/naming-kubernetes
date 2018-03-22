package nl.topicus.naming.etcd;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.naming.Name;
import javax.naming.NamingException;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jboss.logging.Logger;

public class EtcdNamingStore
{
	private static final Logger logger = Logger.getLogger(EtcdNamingStore.class);

	private static final String PREFIX = "java.naming.etcd.prefix";

	private final Client client;

	private final Hashtable<String, Object> envprops;

	private final String prefix;

	private final LoadingCache<Name, Optional<Object>> values;

	public EtcdNamingStore(final Client client, final Hashtable<String, Object> envprops)
	{
		this.client = client;
		this.envprops = envprops;
		prefix = (String) envprops.get(PREFIX);
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
					return Optional.ofNullable(createBinding(name));
				}
			});
		// TODO : async refresh
	}

	private Object createBinding(final Name name) throws InterruptedException, ExecutionException
	{
		ByteSequence value = fetch(name);
		if (value == null)
			return null;

		return coerceToType(value, getType(name));
	}

	private ByteSequence fetch(final Name name) throws InterruptedException, ExecutionException
	{
		String etcdKey = prefix + name.toString();
		logger.debugv("Fetching key ''{0}'' from etcd...", etcdKey);
		// TODO: exceptie indien meerdere keys gevonden ipv 1ste teruggeven
		GetResponse getResponse = client.getKVClient().get(ByteSequence.fromString(etcdKey)).get();
		return getResponse.getKvs().isEmpty() ? null : getResponse.getKvs().get(0).getValue();
	}

	private String getType(final Name name)
	{
		return (String) envprops.get("type." + name.toString());
	}

	/**
	 * Based on org.jboss.as.naming.subsystem.NamingBindingAdd:coerceToType
	 */
	private Object coerceToType(final ByteSequence bValue, final String type)
			throws IllegalArgumentException
	{
		String value = bValue.toStringUtf8();
		if (type == null || type.isEmpty() || type.equals(String.class.getName()))
		{
			return value;
		}
		else if (type.equals("char") || type.equals("java.lang.Character"))
		{
			return value.charAt(0);
		}
		else if (type.equals("byte") || type.equals("java.lang.Byte"))
		{
			return Byte.parseByte(value);
		}
		else if (type.equals("short") || type.equals("java.lang.Short"))
		{
			return Short.parseShort(value);
		}
		else if (type.equals("int") || type.equals("java.lang.Integer"))
		{
			return Integer.parseInt(value);
		}
		else if (type.equals("long") || type.equals("java.lang.Long"))
		{
			return Long.parseLong(value);
		}
		else if (type.equals("float") || type.equals("java.lang.Float"))
		{
			return Float.parseFloat(value.toString());
		}
		else if (type.equals("double") || type.equals("java.lang.Double"))
		{
			return Double.parseDouble(value);
		}
		else if (type.equals("boolean") || type.equals("java.lang.Boolean"))
		{
			return Boolean.parseBoolean(value);
		}
		else if (type.equals(URL.class.getName()))
		{
			try
			{
				return new URL(value);
			}
			catch (MalformedURLException e)
			{
				throw new IllegalArgumentException("Malformed URL '" + value + "'!", e);
			}
		}
		else if (type.equals(X509Certificate.class.getName()))
		{
			try
			{
				final CertificateFactory factory = CertificateFactory.getInstance("X.509");
				return factory.generateCertificate(
					new ByteArrayInputStream(Base64.getMimeDecoder().decode(value.getBytes())));
			}
			catch (CertificateException e)
			{
				throw new IllegalArgumentException("Cannot decode certificate!", e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Unknown binding type '" + type + "'!");
		}
	}

	public Object get(final Name key) throws NamingException
	{
		try
		{
			return values.get(key).orElse(null);
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
