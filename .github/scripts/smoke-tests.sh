#!/bin/bash
DEPLOYMENT_URL=$1
echo "Running smoke tests against: $DEPLOYMENT_URL"

# Test 1: Health Check
echo "Test 1: Health Check"
curl -f "$DEPLOYMENT_URL/actuator/health" || exit 1

# Test 2: Info Endpoint
echo "Test 2: Info Endpoint"
curl -f "$DEPLOYMENT_URL/actuator/info" || exit 1

# Test 3: Metrics Available
echo "Test 3: Metrics Endpoint"
curl -f "$DEPLOYMENT_URL/actuator/metrics" || exit 1

echo "All smoke tests passed!"
