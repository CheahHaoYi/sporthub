# 06 — Performance testing with Apache JMeter

## 1. Why performance-test?

Performance testing studies how a system behaves as workload changes. It can reveal slow queries, limited connection pools, CPU/memory pressure, thread starvation, network delay or a throughput ceiling. It is different from functional testing: a response can be fast but incorrect, or correct but too slow for its intended use.

- **Load testing:** Measure behavior under an expected workload model.
- **Stress testing:** Increase load past the expected range to find the saturation point and observe recovery/failure behavior.
- **Spike testing:** Apply a sudden jump or drop in traffic.
- **Endurance/soak testing:** Keep a workload running for a long time to find leaks or gradual degradation.
- **Virtual user (VU):** A simulated user thread sending requests according to a plan. Five JMeter threads are five simulated request loops, not necessarily five real human users.
- **Concurrency:** How many operations are active at the same time.
- **Throughput:** Completed requests per time unit, commonly requests/second.
- **Latency:** Time before a response begins; JMeter reports connection/latency timing.
- **Response time:** Total observed time from request start through response completion.
- **Percentile:** P90/P95/P99 is the response-time value at or below which that fraction of requests completed. P95 is usually more informative than an average alone because it shows slower tail requests.
- **Bottleneck:** The resource limiting the system's throughput or response time, such as a slow SQL query or exhausted connection pool.

## 2. Apache JMeter parts

- A **Test Plan** is the complete workload definition.
- A **Thread Group** sets virtual-user threads, ramp-up time and loops/duration.
- A **Sampler** performs an action. An HTTP Request sampler sends an HTTP request.
- An **Assertion** checks a response condition, such as requiring HTTP 200.
- A **Listener** summarizes or writes results. Avoid heavy GUI listeners during load runs; write a JTL file and render a report after the run.
- A **CSV Data Set Config** reads rows from a CSV for different test accounts or data.
- **Parameterization** substitutes variables/properties such as host, port and thread count.
- **Correlation** extracts a value from one response (such as a token or generated ID) and uses it in a later request. This is needed when later calls depend on earlier responses.

## 3. Runnable plan and current limitations

The feature plans in `qa/performance/jmeter/` target planned `/facilities`, `/products` and `/checkout` endpoints. The backend currently does not implement those routes, so those plans cannot produce meaningful application workflow performance results. They also default to the missing `qa/performance/test-users.csv` and empty token, and their broad 2xx/4xx response-code assertions do not establish a business result. The recorded 2026-11-05 run returned HTTP 401 for all 10 samples in each of five feature plans.

Two executable plans for an endpoint that exists today:

- `health-smoke.jmx`: one request to `/actuator/health`, a small readiness check before a longer run.
- `health-load-test.jmx`: a configurable low-scale load check against that same endpoint. Defaults are five threads, five-second ramp-up and five iterations per thread. These defaults are a learning exercise, not a production capacity claim.

Both plans assert HTTP 200 and allow host, port and load to be changed through JMeter properties. Actuator health status is publicly reachable under the current Spring Boot security defaults.

## 4. Run a smoke check and a small load check

Start the local database and backend as described in the API guide. Install Apache JMeter 5.6 or later. Then run from the repository root:

```sh
jmeter -n -t qa/performance/jmeter/health-smoke.jmx \
  -Jhost=localhost -Jport=8080 \
  -l /tmp/sporthub-health-smoke.jtl
```

For the default low-scale load run and an HTML report:

```sh
jmeter -n -t qa/performance/jmeter/health-load-test.jmx \
  -Jhost=localhost -Jport=8080 \
  -Jthreads=5 -Jramp=5 -Jloops=5 \
  -l /tmp/sporthub-health-load.jtl -e -o /tmp/sporthub-health-report
```

The output directory must not already exist. A successful run has no failed response assertions and a JMeter summary/HTML report. A failed 401 usually means the backend security behavior changed; connection failures usually mean the server is not running. The plan measures the health path and its current dependency checks only; it says nothing about facility search, login, bookings, inventory or checkout capacity.

## 5. Interpreting results

Review all of these together:

- Error percentage and failed assertions.
- Throughput, while checking it is actually completing valid requests.
- Median and tail response times (P90, P95, P99), not just mean time.
- CPU, memory, application thread pool and database connection usage.
- PostgreSQL query plans, locks and connection pool waits if database routes are later measured.
- Network latency between the load generator and server.

If throughput plateaus while latency and errors rise, a resource may be saturated. Repeat with one controlled change at a time and keep the same environment. Do not call five or one hundred threads “production capacity” without measured workload assumptions and resource monitoring.

## 6. Future SportHub workload model

Once API services and deterministic data setup exist, create separate flows for browsing facilities, viewing slots, logging in, creating a booking, browsing products and checkout. Use synthetic accounts, distinct slots/products and a safe reset process. Parameterize credentials in environment properties/CSV. For conflicting bookings or last-stock purchases, synchronize requests at the start and verify the invariant: no duplicate confirmed slot and no negative stock.

Heavy load runs belong in a scheduled or opt-in workflow, not on every pull request. Start with a small baseline, validate request correctness, then raise concurrency gradually while collecting database and application metrics.

## 7. Exercise

Change `threads` from 5 to 10 and keep ramp-up/loops constant. Before running, predict what will change and what should remain unchanged. Afterward compare total samples, throughput, P95 and error percentage. Do not infer production capacity from this local health endpoint run.
