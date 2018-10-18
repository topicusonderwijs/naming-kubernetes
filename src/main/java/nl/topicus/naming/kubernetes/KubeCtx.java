/**
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package nl.topicus.naming.kubernetes;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.*;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;
import org.jboss.logging.Logger;

public class KubeCtx implements Context, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(KubeCtx.class);

	private final Hashtable<String, Object> envprops;

	private final KubeNamingStore store;

	@SuppressWarnings("unchecked")
	public KubeCtx(final Hashtable< ? , ? > props) throws NamingException
	{
		envprops = props != null ? (Hashtable<String, Object>) props.clone() : new Hashtable<>(0);
		try
		{
			this.store = new KubeNamingStore(createClient(), envprops);
		}
		catch (Exception e)
		{
			NamingException ne = new NamingException("Failed to create Kubernetes store!");
			ne.setRootCause(e);
			throw ne;
		}
	}

	private ApiClient createClient() throws IOException
	{
		Configuration.setDefaultApiClient(Config.defaultClient());
		return Configuration.getDefaultApiClient();
	}

	@Override
	public Object lookup(Name name) throws NamingException
	{
		logger.debugv("Lookup for ''{0}''...", name.toString());
		try
		{
			return store.get(name);
		}
		catch (NamingException e)
		{
			logger.debugv("Lookup for ''{0}'' failed!", name.toString());
			throw e;
		}
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
