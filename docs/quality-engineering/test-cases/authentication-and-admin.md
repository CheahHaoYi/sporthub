# Authentication and administrator test cases

| ID / scenario | Preconditions and data | Steps | Expected result | Method | Status |
|---|---|---|---|---|---|
| AU-01 Login requires email/password | Open login page. | Inspect fields and submit button. | Email and password are required and email uses email input type. | Selenium UI smoke / React test. | Implemented in both current frontend suites. |
| AU-02 Register under-age account | Complete fields with DOB under age 18. | Submit registration form. | Browser shows “You must be 18 or older…” and does not send registration request. | Selenium UI sanity / React test. | Implemented in current frontend suites. |
| AU-03 Valid registration/login | Adult DOB and valid credentials, backend account service active. | Register, then authenticate. | Agreed user/token response; authenticated user reaches customer page. | API + E2E. | Blocked: backend auth routes absent. |
| AU-04 Duplicate registration | Existing account uses same email. | Register again. | Conflict/validation response; original account unchanged. | API/integration. | Blocked. |
| AU-05 Missing or invalid token | Call protected customer route without/with invalid bearer token. | Inspect status and error contract. | Authentication rejected; no private data returned. | API security. | Blocked: backend token filter/routes absent. |
| AU-06 Customer visits admin page | Store a customer role and open `/admin`. | Observe redirect. | React guard redirects to login. | Selenium UI sanity. | Implemented in `PublicUiSmokeTest`; client-only. |
| AU-07 Customer calls admin API directly | Authenticated customer sends admin write request. | Call route without using UI. | Server returns forbidden; data unchanged. | API authorization. | Blocked: admin API/security configuration absent. |
| AU-08 Admin manages a facility | Admin account and facility API available. | Create/update facility then read it. | Changes persist and non-admin access remains denied. | API + E2E. | Blocked. |
