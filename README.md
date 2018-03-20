# naming-etcd

A Java naming context for WildFly using etcd3 as backend for key lookups.

## Configuration
WildFly 11 module configuration (modules/nl/topicus/naming/etcd/main/module.xml):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.0" name="nl.topicus.naming.etcd">
        <resources>
                <resource-root path="naming-etcd-<version>.jar"/>
        </resources>
        <dependencies>
                <module name="javax.api"/>
                <module name="org.jboss.as.naming"/>
        </dependencies>
</module>
```

WildFly 11 subsystem configuration:
```xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
           <bindings>
               <external-context name="java:/etcd" module="nl.topicus.naming.etcd" class="javax.naming.InitialContext" cache="false">
                   <environment>
                       <property name="java.naming.factory.initial" value="nl.topicus.naming.etcd.EtcdCtxFactory"/>
                       <property name="java.naming.provider.url" value="https://127.0.0.1:2379"/>
                   </environment>
               </external-context>
           </bindings>
           <remote-naming/>
       </subsystem>
```
