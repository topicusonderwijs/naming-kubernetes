# naming-kubernetes

A Java naming context for [WildFly](http://wildfly.org/) using [Kubernetes](https://kubernetes.io/) as backend for lookups.

## Configuration
WildFly 11 subsystem configuration:
```xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
	<bindings>
		<external-context name="java:/k8s" module="nl.topicus.naming-kubernetes" class="javax.naming.InitialContext" cache="false">
			<environment>
				<property name="java.naming.factory.initial" value="nl.topicus.naming.kubernetes.KubeCtxFactory"/>
				<property name="java.naming.kubernetes.namespace" value="<kubernetes namespace>"/>
			</environment>
		</external-context>
	</bindings>
	<remote-naming/>
</subsystem>
```

Enable debug logging for naming-kubernetes:
```xml
<logger category="nl.topicus.naming.kubernetes">
	<level name="DEBUG"/>
</logger>
```
