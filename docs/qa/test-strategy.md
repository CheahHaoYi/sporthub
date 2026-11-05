# QA test strategy

| Layer | Tool | Defects targeted |
|---|---|---|
| Unit | TestNG + Mockito | Business-rule boundaries and isolated service behavior |
| Integration | TestNG + Spring TestContext + PostgreSQL Testcontainers | Mappings, migrations, constraints, transactions and locking |
| API | REST Assured + TestNG | Contract, status, auth, error and persistence behavior |
| UI | Selenium + TestNG | Customer/admin workflows and rendered totals |
| Performance | JMeter | Latency, throughput, saturation and competing writes |

Backend runs `*Test.java` with Surefire/TestNG (`mvn test`) and `*IT.java` with Failsafe/TestNG during `mvn verify`. Frontend uses `npm test` for Vitest and `npm run build` for type-checking/bundling. QA automation is a separately runnable Maven project (`mvn test`) that requires a running backend/frontend and Chrome. PostgreSQL, not H2, is used for migrations, constraints and transaction behavior. The current duplicate-slot test is sequential; concurrent service/API behavior remains unimplemented. CI runs the backend, frontend and QA automation jobs independently and in parallel. The migrations and some database tests exist; feature-level API/business tests await booking, cart, checkout and authentication implementations.
