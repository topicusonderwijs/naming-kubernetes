# naming-kubernetes

A Java naming context for [WildFly](http://wildfly.org/) using [Kubernetes](https://kubernetes.io/) as backend for lookups.

JNDI lookups are resolved to configmap or secret keys using the Kubernetes API. Configmaps and secrets should have the following label to be eligible for naming resolution:


```
k8s.naming.topicus.nl/externalcontext: <name>
```

JNDI subcontexts have their own configmap/secret. To distinguish different subcontexts the configmap/secret should get the following annotation with the full subcontext path as value:

```
k8s.naming.topicus.nl/context: <subcontext>
```

All lookups are first tried to resolve as configmap key. If no configmap key exists for the given JNDI name a secret lookup is performed.

## WildFly configuration
The naming-kubernetes module should be placed in the WildFly module directory.

WildFly subsystem configuration:

```xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
	<bindings>
		<external-context name="java:/k8s" module="nl.topicus.naming-kubernetes" class="javax.naming.InitialContext" cache="false">
			<environment>
				<property name="java.naming.factory.initial" value="nl.topicus.naming.kubernetes.KubeCtxFactory"/>
				<property name="java.naming.kubernetes.context" value="<external-context name>"/>
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

## Java types
The values returned from Kubernetes are of the Java type `String`. JNDI values are of the Java type `Object`. To expose values as typed Java objects the following type conversion rules are applied:
- Boolean: a `true` or `false` (case insensitive)
- Integer: a value containing only the characters 0-9 (no negative numbers, no overflow detection)
- String: A string of text

## Compatibility Matrix

|                       | WildFly 13 | WildFly 14  |
|-----------------------|------------|-------------|
| naming-kubernetes 1.x | +          | +           |
| naming-kubernetes 2.x | -          | +           |

## Examples
The following configmap exposes the JNDI name `java://k8s/my.jndi.key` (`k8s` is the name of the external-context and has to be defined as a label on the configmap/secret):

```
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    k8s.naming.topicus.nl/externalcontext: "k8s"
  name: ConfigMapExample
data:
  my.jndi.key: "naming-kubernetes-sample-value"
```

The JNDI name `java://k8s/my.subcontext/my.jndi.key` is exposed by the following configmap:

```
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    k8s.naming.topicus.nl/externalcontext: "k8s"
  annotations:
    k8s.naming.topicus.nl/context: "my.subcontext"
  name: ConfigMapSubContextExample
data:
  my.jndi.key: "naming-kubernetes-sample-value"
``` 
