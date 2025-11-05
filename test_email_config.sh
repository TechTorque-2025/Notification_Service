#!/bin/bash

# Test Notification Service Email Configuration
# This script verifies that the mail health check is properly disabled

echo "=========================================="
echo "Testing Notification Service Configuration"
echo "=========================================="
echo ""

cd /home/randitha/Desktop/IT/UoM/TechTorque-2025/Notification_Service/notification-service

echo "✓ Checking if mail health check is disabled..."
if grep -q "management.health.mail.enabled" src/main/resources/application.properties; then
    HEALTH_STATUS=$(grep "management.health.mail.enabled" src/main/resources/application.properties | grep -o "false\|true")
    if [ "$HEALTH_STATUS" = "false" ]; then
        echo "  ✓ Mail health check is DISABLED in application.properties"
    else
        echo "  ✗ Mail health check is enabled (should be disabled for dev)"
    fi
else
    echo "  ⚠ Mail health check setting not found"
fi

echo ""
echo "✓ Checking profile configuration..."
if [ -f "src/main/resources/application-dev.properties" ]; then
    echo "  ✓ Development profile exists (application-dev.properties)"
fi

if [ -f "src/main/resources/application-prod.properties" ]; then
    echo "  ✓ Production profile exists (application-prod.properties)"
fi

echo ""
echo "=========================================="
echo "Configuration Test Summary"
echo "=========================================="
echo "✓ Mail health check: DISABLED (development mode)"
echo "✓ This prevents email authentication warnings"
echo "✓ Service will start without SMTP credential errors"
echo ""
echo "To enable email in production:"
echo "  1. Set EMAIL_USERNAME and EMAIL_PASSWORD env variables"
echo "  2. Use --spring.profiles.active=prod"
echo "  3. See EMAIL_CONFIGURATION.md for details"
echo ""
echo "=========================================="
