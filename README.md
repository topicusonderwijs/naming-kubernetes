# naming-etcd

A Java naming context for [WildFly](http://wildfly.org/) using [etcd3](https://coreos.com/etcd/) as backend for key lookups.

## Configuration
WildFly 11 subsystem configuration:
```xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
	<bindings>
		<external-context name="java:/etcd" module="nl.topicus.naming-etcd" class="javax.naming.InitialContext" cache="false">
			<environment>
				<property name="java.naming.factory.initial" value="nl.topicus.naming.etcd.EtcdCtxFactory"/>
				<property name="java.naming.provider.url" value="https://127.0.0.1:2379"/>
				<property name="java.naming.etcd.cacert" value="<cafile>"/>
				<property name="java.naming.etcd.prefix" value="/foo/"/>
			</environment>
		</external-context>
	</bindings>
	<remote-naming/>
</subsystem>
```

Enable debug logging for naming-etcd:
```xml
<logger category="nl.topicus.naming.etcd">
	<level name="DEBUG"/>
</logger>
```