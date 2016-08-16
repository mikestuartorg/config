# Configuration Sample

This application demonstrates using a Config Server -- with encrypted properties.

> Note: *You must have the [JCE](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) installed.* 


## Build

Run `gradle build`

## Setup

### Create Config Server 

#### Set Repo URL

Run `scripts/deploy.sh target/config-sample.jar`

```
cf create-service p-config-server standard config-server
read -p "Please visit Apps Manager and enter a repository URI for the new \"config-server\" service. Then press any key to continue..." -n1 -s
echo
cf push -p "$@"
```
The script will create a Config Server instance and then wait for it to be configured; when prompted, visit Apps Manager to configure the instance

* URI: https://github.com/mikestuartorg/config-repo
* Branch:
* Search Paths:
* Username:
* Password:

#### Bind

Then bind the app by going to it and picking the service. Also update the env var SPRING_PROFILES_ACTIVE to "dev,encryption" 
ACTUALLY, SPECIFIY IN MANIFEST (cf restage was failing after UI updates)

```
$ cat manifest.yml 
---
instances: 1
memory: 512M
applications:
  - name: config-sample
    services:
      - config-server
    env:
      SPRING_PROFILES_ACTIVE: dev,encryption
```

When the script has finished, set the `CF_TARGET` environment variable to the API endpoint of your
Elastic Runtime instance (as in `https://sample-config.preprodapp.cfapps.corelogic.net`), then run
`cf restage config-sample` to restage the application so that that change will take effect.
Setting `CF_TARGET` causes Spring Cloud Services to add the the SSL certificate at the specfied API
endpoint to the JVM's truststore, so that the client application can talk to a Config Server service
instance even if your Elastic Runtime instance is using a self-signed SSL certificate
(see the [Config Server documentation](http://docs.pivotal.io/spring-cloud-services/config-server/writing-a-spring-client.html#self-signed-ssl-certificate)).

```
$ cf set-env config-sample CF_TARGET https://sample-config.preprodapp.cfapps.corelogic.net
Setting env variable 'CF_TARGET' to 'https://sample-config.preprodapp.cfapps.corelogic.net' for app config-sample in org Corelogic / space sb as user...
OK
TIP: Use 'cf restage' to ensure your env variable changes take effect
$ cf restage config-sample
```

__NOTE:__ By default, the Config Server client dependency will cause all application endpoints to be secured by HTTP Basic authentication. For more information or if you wish to disable this, http://scs-docs.black.springapps.io/spring-cloud-services/config-server/writing-a-spring-client.html#disable-http-basic-auth[see the documentation]. (HTTP Basic authentication is disabled in this sample application.)

### Encrypting

#### Installing the JCE

Place the JCE unlimited strength jars under `src/main/resources/jce`:

```
$ ls src/main/resources/jce
US_export_policy.jar
local_policy.jar
```

Create a `init.sh` script under `src/main/resources/.profile.d` that will be executed when the
application is first created on a newly provisioned VM:
 
```
$ cat src/main/resources/.profile.d/init.sh 
#!/bin/bash
 
sleep 2
 
ls $HOME/.java-buildpack/open_jdk_jre/lib/security
cp $HOME/jce/* $HOME/.java-buildpack/open_jdk_jre/lib/security
ls $HOME/.java-buildpack/open_jdk_jre/lib/security
```

#### CLI

DevOps command line (less a self-service solution) to encrypt values and paste in properties file:

http://projects.spring.io/spring-cloud/spring-cloud.html#_spring_boot_cloud_cli
http://projects.spring.io/spring-cloud/spring-cloud.html#_encryption_and_decryption_2

For asymmetric:

```
$ keytool -genkeypair -alias config-sample -keyalg RSA -dname "CN=Cloud,OU=Labs,O=CoreLogic,L=Austin,S=TX,C=US" -keystore server.jks -storepass letmein
```

```
$ spring encrypt torchy --key @server.jks
20885e4ba668ad8d959bc5d79b5071b6854e2a49c31a2146d96c87b4dacacd1e

$ spring decrypt --key @server.jks 20885e4ba668ad8d959bc5d79b5071b6854e2a49c31a2146d96c87b4dacacd1e
torchy
```

For symmetric:

```
spring encrypt mysecret --key abc123
678945e480a9b6066b4b6eb2df3779d42cc9bd810bf84b8d8fe6941f182152d4

$ spring decrypt --key abc123 678945e480a9b6066b4b6eb2df3779d42cc9bd810bf84b8d8fe6941f182152d4
mysecret
```

Put the encrypted values into the config-repo properties file, push and refresh. See below for more
details.


### Refreshing

Have a component that is registered to listen for refresh:

```
@RefreshScope
@Component
public class SampleProperties {

  @Value("${sample.secret}")
  String secret;
```

Change property values and then add/commit/push:

```
$ git add sample-config-encryption.properties
$ git commit -m "Updating property values to see refresh"
[master 55d78ed] Updating property values to see refresh
 1 file changed, 2 insertions(+), 2 deletions(-)
 rewrite sample-config-encryption.properties (94%)

$ git push
Counting objects: 3, done.
Delta compression using up to 8 threads.
Compressing objects: 100% (3/3), done.
Writing objects: 100% (3/3), 624 bytes | 0 bytes/s, done.
Total 3 (delta 1), reused 0 (delta 0)
To git@github.com:mikestuartorg/config-repo.git
   a0dee6c..55d78ed  master -> master
```

Then send message to refresh:

```
$ curl -X POST https://sample-config.preprodapp.cfapps.corelogic.net/refresh
```

## Visiting

Visit `[ROUTE]/`, where `[ROUTE]` is the route bound to the application. 
The value will be taken from the configuration repository and the value of `sample.index`.

Visit `[ROUTE]/secret`, where `[ROUTE]` is the route bound to the application. 
The value will be taken from the configuration repository and the value of `sample.secret`.
