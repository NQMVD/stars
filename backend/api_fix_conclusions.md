# Walkthrough - Debugging API Errors

I have successfully identified and fixed the errors preventing the API from working correctly.

## Issues Identified & Fixed

### 1. Database Connection & Prepared Statements
**Issue:** The application was failing with `prepared statement "sqlx_s_1" already exists`. This is a common issue when using `sqlx` with Supabase's Transaction Pooler (port 6543).
**Fix:** 
- Changed the database port in `.env` from `6543` (Transaction Pooler) to `5432` (Session Pooler/Direct).
- Added `.persistent(false)` to `sqlx` queries to ensure compatibility with connection pooling.

### 2. Schema Mismatch (ID Type)
**Issue:** The application expected `app_id` to be a string (e.g., "test-app"), but the database table `desktop_apps` had an `id` column of type `BIGINT` (likely from GitHub data). This caused `operator does not exist: bigint = text` errors.
**Fix:**
- Added a new `slug` column (TEXT) to `desktop_apps` to store the string identifier.
- Updated `App` model in `src/models.rs` to map the `id` field to the `slug` column.
- Updated `src/db.rs` to query by `slug` instead of `id`.
- Updated `releases` table foreign key to reference `desktop_apps(slug)` instead of `id`.

### 3. Invalid Test Data
**Issue:** The test data inserted for "test-app" used dummy values ("owner", "repo") which caused GitHub API 404 errors.
**Fix:**
- Updated `src/bin/fix_schema.rs` to insert real data pointing to the `Nukesor/pueue` repository.

### 4. GitHub Authentication
**Issue:** The GitHub credentials in `.env` were rejected by GitHub API (`401 Bad credentials`).
**Fix:**
- Temporarily disabled Basic Auth in `src/github.rs` to allow fetching public releases without authentication. **Please update your `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` in `.env` to restore authenticated requests.**

## Verification Results

Ran `bash test_api.sh` which performs two requests:

1. **GET /api/apps/test-app/latest**
   - **Result:** `200 OK`
   - **Output:** Returns JSON with latest release info (v4.0.1).

2. **GET /api/apps/test-app/download**
   - **Result:** `200 OK`
   - **Output:** Successfully streams the asset (`pueue-aarch64-apple-darwin`).

## Next Steps
- Update `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` in `.env` with valid credentials.
- Re-enable authentication in `src/github.rs` (uncomment `.basic_auth(...)`).
