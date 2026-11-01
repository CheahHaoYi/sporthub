# JMeter performance plans

Plans are parameterized and live in `jmeter/`. Install Apache JMeter locally; set host, port, API prefix, credentials, CSV paths and test data using JMeter properties rather than committing secrets. A CSV should contain synthetic test accounts only. Never point a stress profile at production.

Run an editable plan in the JMeter GUI, or run and save a report without a GUI:

```sh
jmeter -n -t qa/performance/jmeter/browse-facilities.jmx -Jhost=localhost -Jport=8080 -JapiPrefix=/api/v1 -l /tmp/facilities.jtl -e -o /tmp/facilities-report
```

Inspect error rate, throughput, p95/p99 and server/database resource use together. A climbing latency curve or a rising error rate as throughput plateaus indicates saturation. For contention plans, coordinate requests with a Synchronizing Timer and assert that only one capacity-one booking or last-unit purchase succeeds. The 100 concurrent users, 60-second ramp and 500 ms search p95 are example baselines only; benchmark against the environment before making capacity claims. Heavy runs do not belong in every pull request.

The current `.jmx` files are placeholders; endpoint-specific scenarios require a runnable API and deterministic test-fixture/reset mechanism, which are blocked on the pending database schema/API implementation.
