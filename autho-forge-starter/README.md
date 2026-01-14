# Autho-Forge Starter

A Spring Boot starter for seamless JWT authentication integration with the Autho-Forge authentication server.

## Features

- 🔐 **Automatic JWT validation** using RS256 signatures
- 🔑 **JWK-based public key fetching** with intelligent caching
- 🚀 **Zero-code integration** - just add dependency and configure
- 🔄 **Automatic key rotation support**
- 🛡️ **Spring Security integration** with role-based access control
- ⚡ **Thread-safe** and **production-ready**

## Quick Start

### Option 1: Local Installation (Dev)

First, install the project to your local Maven repository:
```bash
# In the project root
mvn clean install -DskipTests
```

Then add this to your microservice's `pom.xml`:
```xml
<dependency>
    <groupId>com.adarsh.autho</groupId>
    <artifactId>autho-forge-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Option 2: Using JitPack (Remote)

Once you push this project to a public GitHub repository, you can use JitPack to import it without local installation.

1. Add the JitPack repository to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency using your GitHub coordinates:
```xml
<dependency>
    <groupId>com.github.YOUR_GITHUB_USERNAME.adarsh-autho-forge</groupId>
    <artifactId>autho-forge-starter</artifactId>
    <version>TAG_OR_COMMIT_HASH</version>
</dependency>
```

### 2. Configure Properties

Add to your `application.properties`:

```properties
# Required: JWK endpoint of your auth server
autho.forge.jwk-set-uri=http://localhost:8080/.well-known/jwks.json

# Required: Expected issuer in JWT tokens
autho.forge.issuer=https://adarsh-autho-forge

# Optional: Enable/disable authentication (default: true)
autho.forge.enabled=true

# Optional: JWK cache duration in minutes (default: 60)
autho.forge.jwk-cache-duration-minutes=60
```

### 3. That's It! 🎉

Your microservice is now protected. All requests must include a valid JWT token:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
     http://localhost:8081/api/your-endpoint
```

## How It Works

1. **Request Interception**: The `JwtAuthenticationFilter` intercepts all incoming requests
2. **Token Extraction**: Extracts JWT from `Authorization: Bearer <token>` header
3. **Signature Validation**: Fetches public key from JWK endpoint and validates signature
4. **Claims Validation**: Verifies issuer and expiration
5. **Security Context**: Sets Spring Security context with user details and roles

## Accessing User Information

In your controllers, you can access authenticated user information:

```java
@RestController
public class OrderController {
    
    @GetMapping("/orders")
    public List<Order> getOrders() {
        // Get authentication from security context
        JwtAuthenticationToken auth = 
            (JwtAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();
        
        String userId = auth.getUserId();
        String username = auth.getUsername();
        
        // Your business logic here
        return orderService.getOrdersForUser(userId);
    }
}
```

## Role-Based Access Control

The starter automatically extracts roles from JWT claims and adds them as Spring Security authorities:

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/orders/{id}")
public void deleteOrder(@PathVariable Long id) {
    orderService.delete(id);
}
```

## Customizing Security

If you need custom security configuration, you can override the default:

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain customFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {
        
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## Disabling Authentication (Testing)

For local development or testing, you can disable authentication:

```properties
autho.forge.enabled=false
```

## Troubleshooting

### "Public key not found for kid: xxx"

- Ensure your auth server is running and accessible
- Verify `autho.forge.jwk-set-uri` points to the correct JWK endpoint
- Check that the auth server is publishing keys at `/.well-known/jwks.json`

### "Invalid issuer"

- Ensure `autho.forge.issuer` matches the issuer claim in your JWT tokens
- Check your auth server's issuer configuration

### "Token expired"

- The JWT has expired, request a new token from the auth server
- Use the refresh token endpoint to get a new access token

## Architecture

```
┌─────────────────┐
│  Microservice   │
│   (Your App)    │
└────────┬────────┘
         │
         │ 1. Request with JWT
         ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
└────────┬────────────────┘
         │
         │ 2. Extract token
         ▼
┌─────────────────┐
│   JwkService    │◄──── 3. Fetch public key (cached)
└────────┬────────┘
         │
         │ 4. Validate signature
         ▼
┌─────────────────────────┐
│  Spring Security        │
│  Context                │
└─────────────────────────┘
```

## Requirements

- Java 21+
- Spring Boot 4.0+
- Autho-Forge authentication server running

## License

Same as parent project
