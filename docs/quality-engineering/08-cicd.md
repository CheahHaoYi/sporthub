# 08 — CI/CD and automated testing pipeline

## 1. Key terms

- **Git** records changes to source files. A **branch** is a separate line of work.
- A **pull request (PR)** asks teammates to review a branch before it is merged.
- **Continuous Integration (CI)** regularly builds and tests changes together so problems are found early.
- **Continuous Delivery** keeps the software in a releasable state, with a person choosing when to deploy.
- **Continuous Deployment** automatically deploys every change that passes the required checks.
- A **build pipeline** is the ordered set of automated steps that compiles, tests and packages software.
- A **quality gate** is a required check that must pass before merging or release.
- A **test report/artifact** preserves results such as failing methods, logs and coverage.
- A **regression suite** is the selected group of tests rerun after changes to catch regressions.

This repository builds and tests code, but the current workflow does not deploy the product. It is CI, not continuous deployment.

## 2. Current GitHub Actions workflow

[`/.github/workflows/ci.yml`](../../.github/workflows/ci.yml) runs on pushes to `main` and on pull requests. It has three jobs:

1. **Backend:** Java 21 and Maven run `mvn clean verify`. TestNG unit tests run first; Failsafe starts a real PostgreSQL Testcontainer for integration/system checks; JaCoCo writes coverage. Docker must be available to Testcontainers.
2. **Frontend:** Node 22 installs the locked npm dependencies with `npm ci`, runs Vitest and creates the production Vite build.
3. **QA automation:** A PostgreSQL service is started; the workflow starts Spring Boot and the Vite frontend, then runs REST Assured API and Selenium browser smoke/sanity tests. Chrome is configured headlessly. Test reports are uploaded even when a check fails.

The live QA job starts the backend with its default Spring Security behavior. Health status is public; the Springdoc metadata check expects an unauthenticated 401 response. There is no application-specific authentication flow in the backend yet.

The three jobs are independent and can run in parallel; their order in the YAML is not a serial testing pipeline. Each required job must pass for the overall check to pass. A failed assertion, compilation error or build failure returns a non-zero step result. Reviewers can inspect the uploaded report to see which check failed. A protected branch can require these checks before merging; the repository settings control that last policy.

## 3. Why these stages are separated

- Compilation catches syntax/type errors.
- Unit tests check business logic quickly.
- Database integration tests verify migrations, constraints and transactions against PostgreSQL.
- API checks exercise the HTTP boundary.
- Browser checks cover implemented frontend interactions.
- A production frontend build catches TypeScript and bundler errors.
- Load tests are excluded from every PR because they consume resources and need a controlled workload. The nightly workflow runs a low-scale health-endpoint plan; product workflow load tests should be added when business APIs exist.

The backend Maven provider was explicitly set to TestNG because an automatic JUnit Platform choice previously produced a misleading successful build with zero tests. A green CI job should mean tests actually ran; the TestNG reports provide that evidence.

## 4. Read the workflow line by line

In the workflow file:

- `on:` defines the push and pull-request triggers.
- `permissions: contents: read` gives jobs only the repository access needed to check out code.
- `jobs:` declares independent work units that GitHub can schedule in parallel.
- `runs-on: ubuntu-latest` selects the hosted Linux runner.
- `services.postgres` starts a disposable PostgreSQL service for the external API/browser job. The health check waits until PostgreSQL accepts connections.
- `actions/checkout` downloads the source branch.
- `actions/setup-java` and `actions/setup-node` install required runtimes and cache dependency downloads.
- `browser-actions/setup-chrome` provides Chrome for Selenium.
- `npm ci` installs exactly the versions in `package-lock.json`.
- The `nohup ... &` lines start backend/frontend servers in the background; the loops poll real endpoints instead of guessing with a fixed sleep.
- Maven runs the API and browser suite. `BROWSER_HEADLESS=true` means Chrome runs without a visible desktop.
- `if: always()` uploads reports even after a failure, which makes debugging easier.

The pull-request workflow does not run JMeter. The separate scheduled/manual nightly workflow runs the small health-endpoint workload. Performance testing needs a controlled environment and is not part of the pull-request gate.

## 5. Run the same checks locally

From their project directories:

```sh
cd backend && mvn verify
cd frontend && npm test && npm run build
cd qa/automation && mvn test
```

For the independent QA suite, start PostgreSQL, backend and frontend first as shown in `11-learn-by-doing.md`. The QA Maven process uses `SYSTEM_BASE_URL`, `UI_BASE_URL` and `BROWSER_HEADLESS`; the API sanity test expects `/v3/api-docs` to remain protected by the current Spring Security defaults.

## 6. Exercise

Open `.github/workflows/ci.yml`. Find the command that runs the Selenium tests and the `if: always()` line for report upload. Explain why keeping reports after a failure is useful. Then name one high-load performance test that should not run on every PR and explain why.
