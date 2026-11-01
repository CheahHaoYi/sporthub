# JMeter performance plans

Plans are parameterized and live in `jmeter/`. `health-smoke.jmx` and `health-load-test.jmx` target the currently implemented Actuator health endpoint and can be run now. The older facility/product/checkout plans are templates for endpoints that do not exist yet. Install Apache JMeter locally; set host, port, CSV paths and synthetic test data using JMeter properties rather than committing secrets. A CSV should contain synthetic test accounts only. Never point a stress profile at production.

Run an editable plan in the JMeter GUI, or run and save a report without a GUI:

```sh
jmeter -n -t qa/performance/jmeter/health-load-test.jmx -Jhost=localhost -Jport=8080 -Jthreads=5 -Jramp=5 -Jloops=5 -l /tmp/health.jtl -e -o /tmp/health-report
```

Inspect error rate, throughput, p95/p99 and server/database resource use together. A climbing latency curve or a rising error rate as throughput plateaus indicates saturation. For future contention plans, coordinate requests with a Synchronizing Timer and assert that only one capacity-one booking or last-unit purchase succeeds. Any high-user profile is an example until benchmarked against a defined workload. Heavy runs do not belong in every pull request.

The health endpoint plans are executable but are not user-facing product performance tests. Endpoint-specific booking/checkout scenarios require the missing business API and deterministic test-fixture/reset mechanism. The schema migrations are present; the service/API implementation is the blocker.
