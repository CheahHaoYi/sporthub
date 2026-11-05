# Deploying the frontend to GitHub Pages

The `Deploy frontend to GitHub Pages` workflow publishes the Vite production build from `frontend/dist` whenever `main` is updated. It runs the frontend tests before deployment. It builds with the repository path (`/<repository>/`), configures React Router to keep every page beneath that path, and publishes `404.html` as the app shell so nested pages continue to load when refreshed. For this repository, the site root is `https://cheahhaoyi.github.io/sporthub/`; routes such as facilities and products remain under `/sporthub/` and do not use the portfolio site's root paths.

## Enable Pages

In the GitHub repository, open **Settings → Pages → Build and deployment** and choose **GitHub Actions** as the source. Then push to `main` or run the workflow manually from the Actions tab. GitHub displays the published URL in the `github-pages` deployment environment.

## Connect the frontend to an API

GitHub Pages hosts static files only. It does not run the Spring Boot backend or PostgreSQL. The frontend reads `VITE_API_BASE_URL` at build time, so for a working deployed app:

1. Deploy the Spring Boot API and its database to a public hosting service.
2. Configure the API to accept browser requests from the Pages site origin through its CORS settings.
3. In **Settings → Secrets and variables → Actions → Variables**, add a repository variable named `VITE_API_BASE_URL`, with the API base URL including `/api/v1` (for example, `https://api.example.com/api/v1`).
4. Run the Pages workflow again so Vite embeds that URL in the frontend build.

If the variable is unset, the workflow warns and still publishes the static frontend, but the frontend's existing development default (`http://localhost:8080/api/v1`) cannot serve visitors. Authentication, bookings, product data, and other API-backed features therefore need a deployed API to work.
