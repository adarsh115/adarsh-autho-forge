# Quick Integration Guide for OMS Project

## Add to OMS `pom.xml`

```xml
<dependency>
    <groupId>com.adarsh.autho</groupId>
    <artifactId>autho-forge-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configure in OMS `application.properties`

```properties
# Required: JWK endpoint of auth server
autho.forge.jwk-set-uri=http://localhost:8080/.well-known/jwks.json

# Required: Expected issuer in JWT
autho.forge.issuer=https://adarsh-autho-forge
```

## Done! 🎉

All OMS endpoints are now protected. Clients must send:

```
Authorization: Bearer <jwt-token>
```

## Access User Info in Controllers

```java
JwtAuthenticationToken auth = (JwtAuthenticationToken) 
    SecurityContextHolder.getContext().getAuthentication();

String userId = auth.getUserId();
String username = auth.getUsername();
```

## Disable for Testing

```properties
autho.forge.enabled=false
```
