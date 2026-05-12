#!/bin/bash

# TB Care Platform - Authentication Test Script
# Este script testa o fluxo completo de autenticação JWT

set -e

API_URL="http://localhost:8082/api"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "TB Care Platform - Authentication Tests"
echo "=========================================="
echo ""

# Test 1: Health Check (público)
echo -e "${YELLOW}[1/6] Testing public endpoint (health)...${NC}"
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/health")
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Health check passed${NC}"
else
    echo -e "${RED}✗ Health check failed (HTTP $HTTP_CODE)${NC}"
    exit 1
fi
echo ""

# Test 2: Protected endpoint without token (deve falhar)
echo -e "${YELLOW}[2/6] Testing protected endpoint without token...${NC}"
PROTECTED_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/patients")
HTTP_CODE=$(echo "$PROTECTED_RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo -e "${GREEN}✓ Correctly blocked unauthorized access${NC}"
else
    echo -e "${RED}✗ Should have blocked access (HTTP $HTTP_CODE)${NC}"
    exit 1
fi
echo ""

# Test 3: Login com admin
echo -e "${YELLOW}[3/6] Testing login with admin credentials...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "admin@tbcare.com",
        "password": "admin123"
    }')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Login failed - no token received${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Login successful${NC}"
echo "Token: ${TOKEN:0:50}..."
echo ""

# Test 4: Get current user
echo -e "${YELLOW}[4/6] Testing /auth/me endpoint...${NC}"
ME_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/auth/me" \
    -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$ME_RESPONSE" | tail -n1)
BODY=$(echo "$ME_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Successfully retrieved user data${NC}"
    echo "$BODY" | grep -o '"name":"[^"]*' | cut -d'"' -f4
    echo "$BODY" | grep -o '"email":"[^"]*' | cut -d'"' -f4
    echo "$BODY" | grep -o '"role":"[^"]*' | cut -d'"' -f4
else
    echo -e "${RED}✗ Failed to get user data (HTTP $HTTP_CODE)${NC}"
    exit 1
fi
echo ""

# Test 5: Access protected endpoint with token
echo -e "${YELLOW}[5/6] Testing protected endpoint with valid token...${NC}"
PATIENTS_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL/patients" \
    -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$PATIENTS_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Successfully accessed protected endpoint${NC}"
else
    echo -e "${RED}✗ Failed to access protected endpoint (HTTP $HTTP_CODE)${NC}"
    exit 1
fi
echo ""

# Test 6: Login com professional
echo -e "${YELLOW}[6/6] Testing login with professional credentials...${NC}"
PROF_LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "joao@tbcare.com",
        "password": "prof123"
    }')

PROF_TOKEN=$(echo "$PROF_LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$PROF_TOKEN" ]; then
    echo -e "${RED}✗ Professional login failed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Professional login successful${NC}"
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}All tests passed! ✓${NC}"
echo "=========================================="
echo ""
echo "Available tokens:"
echo "Admin: $TOKEN"
echo "Professional: $PROF_TOKEN"
echo ""
echo "Test with:"
echo "curl $API_URL/patients -H \"Authorization: Bearer \$TOKEN\""
