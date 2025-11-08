# Email Configuration Guide for Notification Service

## Development Mode (Default)

By default, the service runs with the **dev profile** which **disables email health checks**. This prevents authentication failures during local development.

### Running in Development Mode

```bash
# Using Maven
mvn spring-boot:run

# Using IDE - the dev profile is active by default
# Just run NotificationServiceApplication.java
```

**Note:** Email sending will be attempted but won't fail the health check if credentials are invalid.

---

## Testing Email Functionality

If you want to test actual email sending during development, you need valid Gmail credentials with an App Password.

### Step 1: Generate Gmail App Password

1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to **Security**
3. Enable **2-Step Verification** (if not already enabled)
4. Go to **App Passwords**: https://myaccount.google.com/apppasswords
5. Generate a new app password for "Mail"
6. Copy the 16-character password

### Step 2: Set Environment Variables

```bash
# Linux/Mac
export EMAIL_USERNAME="your-email@gmail.com"
export EMAIL_PASSWORD="your-16-char-app-password"

# Windows (PowerShell)
$env:EMAIL_USERNAME="your-email@gmail.com"
$env:EMAIL_PASSWORD="your-16-char-app-password"
```

### Step 3: Enable Mail Health Check (Optional)

In `application-dev.properties`, change:
```properties
management.health.mail.enabled=true
```

### Step 4: Run the Service

```bash
mvn spring-boot:run
```

---

## Production Mode

For production deployments, use the **prod profile** with proper credentials:

```bash
# Set all required environment variables
export SPRING_PROFILE=prod
export DB_URL=jdbc:postgresql://prod-host:5432/notification_db
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_password
export EMAIL_USERNAME=noreply@techtorque.com
export EMAIL_PASSWORD=production-app-password

# Run the application
java -jar notification-service.jar --spring.profiles.active=prod
```

**Production profile automatically:**
- Enables mail health checks
- Uses environment variables for sensitive data
- Reduces logging verbosity
- Sets JPA to validate-only mode

---

## Troubleshooting

### Issue: "Username and Password not accepted"

**Cause:** Invalid Gmail credentials or regular password used instead of App Password.

**Solutions:**
1. Generate an App Password (see above)
2. Disable mail health check: `management.health.mail.enabled=false`
3. Use dev profile (mail health check disabled by default)

### Issue: "535-5.7.8 BadCredentials"

**Cause:** Gmail blocking login attempt.

**Solutions:**
1. Ensure 2-Step Verification is enabled
2. Use an App Password, not your regular password
3. Check if "Less secure app access" is required (deprecated by Google)

### Issue: Email sending works but health check fails

**Cause:** Transient connection issues or rate limiting.

**Solution:** Disable health check in development:
```properties
management.health.mail.enabled=false
```

---

## Configuration Summary

| Profile | Mail Health Check | Use Case |
|---------|------------------|----------|
| **dev** (default) | Disabled | Local development without email |
| **prod** | Enabled | Production with valid credentials |

---

## Quick Commands

```bash
# Development (no email credentials needed)
mvn spring-boot:run

# Development with email testing
export EMAIL_USERNAME="your@gmail.com"
export EMAIL_PASSWORD="app-password"
mvn spring-boot:run

# Production
export SPRING_PROFILE=prod
export EMAIL_USERNAME="prod@techtorque.com"
export EMAIL_PASSWORD="prod-password"
java -jar notification-service.jar
```

---

## Security Notes

⚠️ **Never commit email credentials to version control!**

- Always use environment variables
- Add `.env` files to `.gitignore`
- Use secret management in production (AWS Secrets Manager, Azure Key Vault, etc.)
- Rotate credentials regularly

---

## Alternative: Using MailHog for Development

For local email testing without real credentials:

```bash
# Start MailHog
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Update application-dev.properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
management.health.mail.enabled=false

# Access web UI at http://localhost:8025
```

This captures all emails locally without sending them to real addresses.
