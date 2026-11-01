# QA test strategy

| Layer | Tool | Defects targeted |
|---|---|---|
| Unit | TestNG + Mockito | Business-rule boundaries and isolated service behavior |
| Integration | TestNG + Spring TestContext + PostgreSQL Testcontainers | Mappings, migrations, constraints, transactions and locking |
| API | REST Assured + TestNG | Contract, status, auth, error and persistence behavior |
| UI | Selenium + TestNG | Customer/admin workflows and rendered totals |
| Performance | JMeter | Latency, throughput, saturation and competing writes |

Backend runs `*Test.java` with Surefire and TestNG and `*IT.java` with Failsafe. The Maven suite files keep these suites separate. QA automation is a separately runnable Maven project. PostgreSQL, not H2, is required for database/concurrency behavior. Current Java tests are an initial setup; schema-driven suites await completed database migrations and route implementation.
