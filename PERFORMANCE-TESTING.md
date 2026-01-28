# Performance Testing Guide

## Overview

This document describes the performance testing strategy for the EcoGo application using Apache JMeter.

## JMeter Test Configuration

### Load Test Parameters

- **Thread Count:** 50 concurrent users
- **Ramp-up Period:** 10 seconds
- **Loop Count:** 100 iterations per user
- **Total Requests:** ~15,000 requests

### Target Endpoints

The load test targets the following Spring Boot Actuator endpoints:

1. **Health Check:** `/actuator/health`
   - Validates application availability
   - Expected response time: < 100ms

2. **Metrics Endpoint:** `/actuator/metrics`
   - Retrieves application metrics
   - Expected response time: < 150ms

3. **Info Endpoint:** `/actuator/info`
   - Returns application information
   - Expected response time: < 100ms

### Think Time

- **Constant Timer:** 100ms between requests
- Simulates realistic user behavior

## Performance Baselines

### Response Time Targets

| Metric | Target | Critical |
|--------|--------|----------|
| Average Response Time | < 150ms | < 300ms |
| 95th Percentile (p95) | < 200ms | < 500ms |
| 99th Percentile (p99) | < 300ms | < 1000ms |
| Max Response Time | < 500ms | < 2000ms |

### Throughput Targets

- **Minimum Throughput:** 100 requests/second
- **Target Throughput:** 150 requests/second
- **Peak Throughput:** 200 requests/second

### Error Rate

- **Target:** < 0.1% error rate
- **Maximum Acceptable:** < 1% error rate

### Resource Utilization

- **CPU Usage:** < 70% under load
- **Memory Usage:** < 80% of available memory
- **Database Connections:** < 80% of pool size

## Running Performance Tests

### Local Execution

```bash
# Navigate to project root
cd /path/to/EcoGo

# Ensure application is running
java -jar target/EcoGo-*.jar

# Run JMeter test
docker run --network="host" \
  -v $(pwd)/performance-tests:/tests \
  justb4/jmeter \
  -n -t /tests/load-test.jmx \
  -l /tests/results.jtl \
  -e -o /tests/report
```

### CI/CD Execution

Performance tests automatically run in the CI/CD pipeline on:
- `develop` branch pushes
- `feature/**` branch pushes (for cicdfeature)

The tests execute after:
1. Integration tests complete
2. Smoke tests pass
3. Application is deployed to staging

### Viewing Results

#### HTML Report

After running tests, view the HTML report:

```bash
# Open the report in browser
open performance-tests/report/index.html
```

The report includes:
- **Dashboard:** Overall test summary
- **Statistics:** Detailed metrics per request
- **Graphs:** Response time distribution, throughput over time
- **Error Analysis:** Failed requests and reasons

#### JTL File Analysis

The raw results file (`results.jtl`) can be analyzed:

```bash
# View summary statistics
cat performance-tests/results.jtl | grep -v "^timestamp" | wc -l

# Calculate average response time
awk -F',' 'NR>1 {sum+=$2; count++} END {print "Avg Response Time:", sum/count, "ms"}' \
  performance-tests/results.jtl
```

## Test Scenarios

### Scenario 1: Health Check Load

Simulates monitoring systems constantly checking application health.

- **Purpose:** Ensure health endpoint can handle high frequency
- **Load:** 50 concurrent users
- **Duration:** ~10 minutes

### Scenario 2: Mixed Workload

Simulates realistic user behavior across multiple endpoints.

- **Purpose:** Validate application under typical production load
- **Mix:** 40% health, 30% metrics, 30% info
- **Load:** Progressive from 10 to 50 users

### Scenario 3: Stress Test

Pushes application beyond normal limits to find breaking point.

- **Purpose:** Identify maximum capacity
- **Load:** Ramp up to 100+ users
- **Duration:** Until failure or stabilization

## Interpreting Results

### Good Performance Indicators

✅ Response times consistently below targets
✅ Linear throughput scaling with load
✅ No errors or timeouts
✅ Stable memory and CPU usage
✅ Consistent performance across all endpoints

### Warning Signs

⚠️ Response times increasing over time (memory leak?)
⚠️ Sporadic timeouts (resource contention?)
⚠️ High CPU usage (inefficient code?)
⚠️ Error rate > 0.5%
⚠️ Throughput plateaus before expected load

### Critical Issues

❌ Error rate > 1%
❌ Response times > 1 second
❌ Application crashes under load
❌ Memory leaks causing OOM errors
❌ Database connection pool exhaustion

## Troubleshooting

### High Response Times

**Possible Causes:**
- Database query optimization needed
- Insufficient connection pool size
- Network latency issues
- Resource contention

**Solutions:**
- Add database indexes
- Increase connection pool size
- Enable query caching
- Add more application instances

### Memory Issues

**Possible Causes:**
- Memory leaks
- Large object retention
- Insufficient heap size

**Solutions:**
- Profile with JProfiler or VisualVM
- Adjust JVM heap settings: `-Xmx512m -Xms256m`
- Review object lifecycle

### Connection Errors

**Possible Causes:**
- Connection pool exhausted
- Database max connections reached
- Network issues

**Solutions:**
- Increase database connection pool
- Review connection timeout settings
- Add connection retry logic

## Continuous Improvement

### Performance Monitoring

1. **Baseline Establishment**
   - Run tests on every release
   - Track metrics over time
   - Set alerts for regression

2. **Trend Analysis**
   - Compare results between versions
   - Identify performance degradation
   - Correlate with code changes

3. **Capacity Planning**
   - Use load test data for infrastructure sizing
   - Plan for growth (2x, 5x, 10x traffic)
   - Identify scaling bottlenecks

### Best Practices

- **Run Regularly:** Weekly performance tests
- **Version Control:** Store JMX files in git
- **Document Changes:** Note performance impact of features
- **Automate Analysis:** Script result parsing and alerts
- **Share Results:** Publish reports for team visibility

## Advanced Topics

### Custom Test Plans

To test specific API endpoints:

```xml
<!-- Add to load-test.jmx -->
<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.domain">localhost</stringProp>
  <stringProp name="HTTPSampler.port">8090</stringProp>
  <stringProp name="HTTPSampler.path">/api/v1/your-endpoint</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSamplerProxy>
```

### Distributed Testing

For very high loads, use JMeter distributed mode:

```bash
# On master
jmeter -n -t test.jmx -R server1,server2,server3

# On each server
jmeter-server
```

### Integration with Monitoring

Correlate JMeter results with:
- **Prometheus:** Application metrics
- **Grafana:** Real-time dashboards
- **APM Tools:** New Relic, DataDog, etc.

## References

- [Apache JMeter Documentation](https://jmeter.apache.org/usermanual/index.html)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Spring Boot Performance Tuning](https://spring.io/guides/gs/testing-web/)

## Support

For questions or issues with performance testing:
- Check GitHub Issues
- Review CI/CD logs in GitHub Actions
- Consult with DevOps team

---

**Last Updated:** 2026-01-28
**Version:** 1.0
