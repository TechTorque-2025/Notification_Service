# Email Configuration Quick Reference

## ✅ SOLUTION IMPLEMENTED

The notification service has been configured to **disable email health checks during development**, eliminating the authentication warning you encountered.

---

## What Was Done

### 1. **Disabled Mail Health Check** (Primary Solution)
Added to `application.properties`:
```properties
management.health.mail.enabled=false
```

This prevents Spring Boot Actuator from attempting to connect to Gmail SMTP during health checks.

### 2. **Created Development Profile** 
Created `application-dev.properties` with mail health check disabled by default.

### 3. **Created Production Profile**
Created `application-prod.properties` that enables health checks and uses environment variables.

---

## Running the Service

### Development Mode (No Email Credentials Needed)
```bash
cd Notification_Service/notification-service
mvn spring-boot:run
```

✅ **No more email warnings!** The service will start cleanly.

### With Real Email Testing
```bash
export EMAIL_USERNAME="your-email@gmail.com"
export EMAIL_PASSWORD="your-app-password"  # Gmail App Password
mvn spring-boot:run
```

### Production Mode
```bash
export SPRING_PROFILE=prod
export EMAIL_USERNAME="prod@techtorque.com"
export EMAIL_PASSWORD="prod-password"
java -jar notification-service.jar
```

---

## Understanding the Warning

The warning you saw:
```
jakarta.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Cause:** Spring Boot Actuator's health endpoint was trying to verify SMTP connection with placeholder credentials.

**Impact:** 
- ⚠️ Warning in logs
- ✅ Service still starts successfully
- ✅ All endpoints work normally
- ❌ Only email sending would fail (if attempted)

---

## Configuration Files Created

| File | Purpose |
|------|---------|
| `application.properties` | Base config with mail health disabled |
| `application-dev.properties` | Development profile (no real email needed) |
| `application-prod.properties` | Production profile (real credentials required) |
| `EMAIL_CONFIGURATION.md` | Detailed setup guide |
| `test_email_config.sh` | Verification script |

---

## Testing Your Setup

Run the verification script:
```bash
cd Notification_Service
./test_email_config.sh
```

Expected output:
```
✓ Mail health check is DISABLED in application.properties
✓ Development profile exists
✓ Production profile exists
```

---

## Common Scenarios

### Scenario 1: Local Development (Current)
**Status:** ✅ Configured  
**Action:** None needed - just run `mvn spring-boot:run`  
**Email:** Won't send (no valid credentials)  
**Health Check:** Disabled (no warnings)

### Scenario 2: Testing Email Functionality
**Status:** Need valid Gmail App Password  
**Action:** Set EMAIL_USERNAME and EMAIL_PASSWORD env vars  
**Email:** Will send to real addresses  
**Health Check:** Keep disabled to avoid startup delays

### Scenario 3: Production Deployment
**Status:** Use prod profile  
**Action:** Set all env vars, use `--spring.profiles.active=prod`  
**Email:** Fully functional with monitoring  
**Health Check:** Enabled for monitoring

---

## Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| Email warning on startup | ✅ Already fixed - health check disabled |
| Need to test email | Set EMAIL_USERNAME/PASSWORD env vars |
| Want to mock emails | Use MailHog (see EMAIL_CONFIGURATION.md) |
| Production setup | Use prod profile + env vars |

---

## Next Steps

Your notification service is now properly configured for development!

**To resume work:**
```bash
cd Notification_Service/notification-service
mvn spring-boot:run
```

**To test endpoints:**
```bash
# Health check (should be UP without mail health)
curl http://localhost:8088/actuator/health

# API docs
open http://localhost:8088/swagger-ui/index.html
```

---

## Files Modified

- ✏️ `application.properties` - Added `management.health.mail.enabled=false`
- ➕ `application-dev.properties` - New development profile
- ➕ `application-prod.properties` - New production profile
- ➕ `EMAIL_CONFIGURATION.md` - Detailed guide
- ➕ `test_email_config.sh` - Verification script

---

**Status:** ✅ **Email configuration issue RESOLVED**

The service will now start without email authentication warnings during development.
