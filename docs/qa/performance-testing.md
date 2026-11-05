# Performance testing runbook

The detailed explanation of workload terms and current plan limits is in the [JMeter lesson](../quality-engineering/06-performance-testing-jmeter.md). Seven plans live in `qa/performance/jmeter/`: two target implemented health and five are feature templates blocked by missing backend APIs, auth and fixtures.

## Health smoke

With the backend running on port 8080 and JMeter 5.6+, from the repository root:

```sh
jmeter -n -t qa/performance/jmeter/health-smoke.jmx \
  -Jhost=localhost -Jport=8080 \
  -l /tmp/sporthub-health-smoke.jtl
```

This sends one `GET /actuator/health` and asserts HTTP 200. It checks reachability only.

## Low-scale health workload

```sh
jmeter -n -t qa/performance/jmeter/health-load-test.jmx \
  -Jhost=localhost -Jport=8080 \
  -Jthreads=5 -Jramp=5 -Jloops=5 \
  -l /tmp/sporthub-health-load.jtl \
  -e -o /tmp/sporthub-health-report
```

Defaults are five threads, a five-second ramp and five loops (25 requests). Keep JTL and HTML output outside the repository. Inspect each JTL sample's `success`, response code and assertion failure message; a zero JMeter process exit code alone does not prove every sample passed. Compare error rate, throughput, P95/P99 and server/resource behavior. This is a small health-endpoint exercise, not a user-facing capacity benchmark.

## Feature-plan blockers

`browse-facilities.jmx`, `product-search.jmx`, `checkout-load-test.jmx`, `concurrent-booking.jmx` and `concurrent-purchase.jmx` target unimplemented routes. They also refer by default to the absent `qa/performance/test-users.csv`, send `Authorization: Bearer ${token}` with an empty default token, and use broad status assertions rather than business outcomes. The 2026-11-05 recorded run returned HTTP 401 for all 10 samples in each feature plan. Do not interpret those results as feature performance coverage.

Before enabling them: agree the endpoint contract; implement the behavior; create deterministic synthetic fixtures and nonempty auth setup; supply request bodies; use a low safe profile; assert the intended outcome; and restrict runs to an explicitly protected test environment. Never run high-load or destructive workloads against production.
