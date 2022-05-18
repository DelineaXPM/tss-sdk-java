# The Delinea Secret Server Java SDK

![Deploy](https://github.com/thycotic/tss-sdk-java/workflows/Deploy/badge.svg)

The [Delinea](https://delinea.com/) Secret Server Java SDK contains classes that
interact with [Secret Server](https://delinea.com/products/secret-server/) via the REST API.

The SDK contains an API based the [Spring Framework](https://spring.io/projects/spring-framework)
[RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html),
and a simple application based on [Spring Boot](https://spring.io/projects/spring-boot),
that calls the API.

## Install into your application

You can use this SDk in your application by adding the following dependency:

```xml
<dependency>
  <groupId>com.thycotic.secrets</groupId>
  <artifactId>tss-sdk-java</artifactId>
  <version>1.0</version>
</dependency>
```

## Build locally

### Prerequisites

The SDK builds and runs on Java 8 or later.

Apache [Maven](https://maven.apache.org/) is also required to build the SDK.

Maven runs unit and integration tests during the build so the settings in
`src/main/resources/application.properties` must be configured before the build
will succeed.

### Settings

The API authenticates to Secret Server as an _Application User_.
The SDK application gets a secret from Secret Server by it's _id_.

Either `secret_server.tenant` or both `secret_server.api_root_url` and `secret_server.oauth2.token_url` must be set.
`secret_server.tenant` simplifies the configuration when accessing Secret Server Cloud by using template URLs that
assume the default folder structure and parameterize the tenant.

```ini
secret_server.tenant = mytenant
# or
# secret_server.api_root_url = https://mysecretserver/SecretServer/api/v1
# secret_server.oauth2.token_url = https://mysecretserver/SecretServer/oauth2/token
secret_server.oauth2.username = app_user
secret_server.oauth2.password = app_user_password
```

When the `tenant` is set, the API assumes a the top-level domain (TLD) of _com_
but it can be overridden:

```ini
secret_server.tld = com
```

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

    secret.getItems().forEach(item -> {
        if (item.getFieldName().equals("password")) {
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
applicationContext.refresh();

// Fetch the secret
final Secret secret = applicationContext.getBean(SecretServer.class).getSecret(serverSecret.getId());
```
