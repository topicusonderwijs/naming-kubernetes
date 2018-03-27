package nl.topicus.naming.etcd;

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

	private final String prefix;

	private final LoadingCache<Name, Optional<Object>> values;

	public EtcdNamingStore(final Client client, final Hashtable<String, Object> envprops)
	{
		this.client = client;
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
		return value == null ? null : ByteSequenceConverter.coerceToType(value);
	}

	private ByteSequence fetch(final Name name) throws InterruptedException, ExecutionException
	{
		String etcdKey = prefix + name.toString();
		logger.debugv("Fetching key ''{0}'' from etcd...", etcdKey);
		// TODO: exceptie indien meerdere keys gevonden ipv 1ste teruggeven
		GetResponse getResponse = client.getKVClient().get(ByteSequence.fromString(etcdKey)).get();
		return getResponse.getKvs().isEmpty() ? null : getResponse.getKvs().get(0).getValue();
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
