# The Delinea Secret Server and Platform Java SDK

![Deploy](https://github.com/DelineaXPM/tss-sdk-java/workflows/Deploy/badge.svg)

The [Delinea](https://delinea.com/) Secret Server and Platform Java SDK contains classes that
interact with [Secret Server](https://delinea.com/products/secret-server/) and Delinea Platform via the REST API.

The SDK contains an API based the [Spring Framework](https://spring.io/projects/spring-framework)
[RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html),
and a simple application based on [Spring Boot](https://spring.io/projects/spring-boot),
that calls the API.

## Install into your application

You can use this SDK in your application by adding the following dependency:

```xml
<dependency>
  <groupId>com.delinea.secrets</groupId>
  <artifactId>tss-sdk-java</artifactId>
  <version>2.1.0</version>
</dependency>
```

## Build locally

### Prerequisites

The SDK builds and runs on Java 17 or later.

Apache [Maven](https://maven.apache.org/) is also required to build the SDK.

Maven runs unit and integration tests during the build so the settings in
`src/main/resources/application.properties` must be configured before the build
will succeed.

### Settings

The API authenticates to Secret Server using  _Application User_  and with Delinea Platform using  _Service User_  .
The SDK application gets a secret from Secret Server by its  _id_ .

### Secret Server Integration
##### 1. Using Secret Server credentials

`authentication_mode` and `server_url` must be set.

Set authentication_mode to 0 for fetch secret using Secret Server credentials, Set the following properties in application.properties:

```ini
authentication_mode =0
server_url =Secret_Server_url
api_version=v1

server.username =application_user
server.password =application_user_password 
```

##### 2. Using the Client Onboarding Key
To fetch secret using the Client Onboarding, you need to create a new onboarding rule and use an onboarding key for authentication.
1. Go to Secret Server > Settings > All settings and click on SDK Client.
2. Click the Client Onboarding tab, then the Create rule.
3. Enter a name for the new rule(this will be your rule_name).
4. Check the Require onboarding key box.
5. Click Save to auto-generate an onboarding key.
6. You can see the key,select the Show key option (this will be your onboarding_key).

Set authentication_mode to 1 for fetch secret using SDK client, Set the following properties in application.properties:

```ini
authentication_mode =1
server_url =Secret_Server_url
api_version=v1

rule_name =create_rule_name
onboarding_key =onboarding_key
```

### Delinea Platform Integration
##### 1. Using Delinea PLatform credentials

`authentication_mode` and `server_url` must be set.

Set authentication_mode to 0 for fetch secret using Delinea Platform credentials, Set the following properties in application.properties:

```ini
authentication_mode =0
server_url =Delinea_Platform_url
api_version=v1

server.username =service_user
server.password =service_user_password
```
##### 2. Using the Client Onboarding Key
To fetch secret using the Client Onboarding, you need to create a new onboarding rule and use an onboarding key for authentication.
1. Go to Delinea Platform > Settings > Secret Server > Administration > Tools and integrations > click on SDK Client.
2. Click the Client Onboarding tab, then the Create rule.
3. Enter a name for the new rule(this will be your rule_name).
4. Check the Require onboarding key box.
5. Click Save to auto-generate an onboarding key.
6. You can see the key, select the Show key option (this will be your onboarding_key).

Set authentication_mode=1 to fetch secrets using the SDK client.
Set server_url to the Secret Server URL. To find the Secret Server URL in Delinea Platform, go to Settings > Secret Server > Secret Server connection and copy the Secret Server URL.
Set the following properties in application.properties:

```ini
authentication_mode =1
server_url =Secret_Server_url
api_version=v1

rule_name =create_rule_name
onboarding_key =onboarding_key
```

## (Optional) Proxy Configuration
```ini
proxy.host = Proxy server hostname or IP address  
proxy.port = Proxy server port number  
proxy.username = Proxy server username (if authentication is required)  
proxy.password = Proxy server password (if authentication is required)
```

Note: Leave proxy.username and proxy.password blank if your proxy does not require authentication.

## Run the jar

After the SDK application settings are configured the jar can be built:

```bash
mvn package
```

However, the build runs the SDK application which requires a `secret.id`
property:

```ini
secret.id = 1
```

The build also produces an executable jar capable of accepting properties via
the command-line. For example:

```bash
java -jar target/tss-sdk-java-1.0-SNAPSHOT-exec.jar --secret.id=1
```

## Use the API

Configure the `SecretServerFactoryBean` in the Spring
[ApplicationContext](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/ApplicationContext.html)
then inject `SecretServer` where required.

This simple example assumes that the `SecretServerFactoryBean` was configured
externally thus allowing a `SecretServer` instance to be injected automatically.

```java
@Autowired
private SecretServer secretServer;

public static void main(final String[] args) {
    final Secret secret = secretServer.getSecret(1);

    secret.getFields().forEach(item -> {
			if (item.getFieldName().equalsIgnoreCase("password")) {
				System.out.println(String.format("The password is %s", item.getValue()));
			}
    });
}
```

This one creates an [AnnotationConfigApplicationContext](https://docs.spring.io/spring-framework/docs/current/javadoc-api/index.html?org/springframework/context/ApplicationContext.html) then configures
the `SecretServerFactoryBean` from an in-place properties map then registers it
and finally uses it to get a `SecretServer` instance to fetch the `Secret`.
It could be used to adapt the API to a non-Spring Java application or to integrate
with an application environment that provides a configuration store.

```java
final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

// create a new Spring ApplicationContext using a Map as the PropertySource
properties.put("example.property", computedValue());
// ...

applicationContext.getEnvironment().getPropertySources()
        .addLast(new MapPropertySource("properties", properties));

// Register the factoryBean
applicationContext.registerBean(SecretServerFactoryBean.class);
applicationContext.registerBean(AuthenticationService.class);
applicationContext.refresh();

// Fetch the secret
final Secret secret = applicationContext.getBean(SecretServer.class).getSecret(serverSecret.getId());
```
