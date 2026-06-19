# AGENTS.md

This file is the working guide for coding agents in this repository. Keep it current when architecture, deployment, security rules, or validation commands change.

## Ground Truth

- Prefer current source, config, package manifests, and deploy scripts over older docs.
- `README.md` is the human-facing project overview. This file is the stricter operating guide for coding agents.
- Do not rely on stale PID files or old deployment notes for production state. Check the running host when the task is about deployment, DB, OSS, or service health.
- This repo has two frontend surfaces. Always distinguish `frontend/` (admin console) from `front/` (public tender portal).

## Project Snapshot

- Project: 招标系统, with a Spring Boot backend, an admin frontend, and a public tender portal.
- Backend: Java 8, Spring Boot 2.7.18, Maven, Spring Security + JWT, Spring Data JPA, MySQL 8, springdoc-openapi/Swagger UI.
- Main backend artifact: `target/zhaobiao-admin.jar`.
- Main backend package: `src/main/java/com/zhaobiao/admin`.
- Backend tests: `src/test/java/com/zhaobiao/admin`.
- Test DB: Docker MySQL 8 from `src/test/resources/application.yml`, defaulting to `zhaobiao_admin_test`.
- File storage: local filesystem by default, Aliyun OSS when `APP_FILE_TYPE=oss`.
- Admin frontend: `frontend/`, Vue 2.6 + Vue CLI 3, View Design/iView-style admin template, production base `/ztbgl/`.
- Public portal: `front/`, Vue 3.2 + Vite 2, production base `/ztbfb/`.
- Spring Boot can serve tracked static files from `src/main/resources/static`, but the captured Nginx config deploys the two frontend apps separately.

## Repository Layout

- `src/main/java/com/zhaobiao/admin/controller`: REST controllers.
- `src/main/java/com/zhaobiao/admin/service`: business logic and transaction boundaries.
- `src/main/java/com/zhaobiao/admin/repository`: Spring Data JPA repositories.
- `src/main/java/com/zhaobiao/admin/entity`: JPA entities and table mappings.
- `src/main/java/com/zhaobiao/admin/dto`: request/response DTOs, grouped by domain.
- `src/main/java/com/zhaobiao/admin/mapper`: entity-to-DTO mapping helpers.
- `src/main/java/com/zhaobiao/admin/security`: JWT auth, admin/member principals, security handlers.
- `src/main/java/com/zhaobiao/admin/config`: application config, seed data, storage/JWT/bootstrap/prod validators.
- `src/main/java/com/zhaobiao/admin/logging`: operation-log aspect and annotation.
- `frontend/`: admin management UI.
- `front/`: public tender portal UI.
- `sql/mysql8/data-initializer.sql`: manual MySQL seed script derived from `DataInitializer`.
- `sql/mysql8/2026-05-07-portal-member-profile-fields.sql`: production schema migration for member first-login and profile-file fields.
- `sql/mysql8/2026-05-23-complete-release.sql`: complete 2026-05-23 migration for test/prod release validation.
- `sql/mysql8/2026-05-23-news-module.sql`: focused migration for the news module and tender publish permission.
- `sql/mysql8/2026-05-23-news-publish-permission.sql`: incremental migration to add/fix `NEWS_PUBLISH_BUTTON` after the complete script has already been run.
- `docs/production-db-schema-change-list.md`: production-facing schema/data migration notes.
- `docs/frontend-change-guide-2026-05-23.md` and `.html`: frontend handoff for the 2026-05-23 backend contracts.
- `DEPLOY_LINUX.md`: generic Linux backend deployment guide; verify against the real server before operating.
- `nginx.conf.from-server`: captured Nginx config for `/ztbgl/` and `/ztbfb/`.
- Historical analysis docs such as `backend-change-analysis.md`, `backend-dev-task-list.md`, and `backend-quality-report-2026-04-16-qa.md` are useful context, not current authority.

## Backend Runtime

Default backend URL:

- API root: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Important environment variables:

- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `MYSQL_TEST_DB` defaults to `zhaobiao_admin_test` for backend tests.
- `MYSQL_CONTEXT_TEST_DB` defaults to `zhaobiao_admin_context_test` for the standalone context-load test.
- `APP_JWT_SECRET` must be present and at least 32 characters.
- `APP_BOOTSTRAP_ADMIN_PASSWORD` is only used if `DataInitializer` is explicitly registered; current main application startup does not auto-register it.
- `SPRING_PROFILES_ACTIVE=prod` enables production database safety validation.
- `APP_FILE_TYPE=local|oss`
- Local storage: `APP_FILE_STORAGE_PATH`, `APP_FILE_TEMP_PATH`
- OSS storage: `APP_FILE_OSS_BUCKET`, `APP_FILE_OSS_ENDPOINT`, `APP_FILE_OSS_CREDENTIAL_MODE`, `APP_FILE_OSS_ROLE_NAME`, `APP_FILE_OSS_ACCESS_KEY_ID`, `APP_FILE_OSS_ACCESS_KEY_SECRET`, `APP_FILE_OSS_KEY_PREFIX`

Local run example:

```bash
APP_JWT_SECRET=ChangeThisToAVeryLongRandomSecret123456 \
mvn spring-boot:run
```

Empty MySQL initialization:

- Current `DataInitializer` is not annotated as a Spring bean and is imported only by integration tests.
- For a fresh MySQL database, create the schema first, then run `sql/mysql8/data-initializer.sql` or deliberately register the initializer before relying on `APP_BOOTSTRAP_ADMIN_PASSWORD`.

Backend validation:

```bash
mvn test
```

Package backend:

```bash
mvn clean package -DskipTests
```

## Database Rules

- Default database name: `zhaobiao_admin`.
- Main config uses MySQL with `spring.jpa.hibernate.ddl-auto=update`.
- Tests use Docker MySQL; `src/test/resources/schema.sql` resets the schema and Hibernate `ddl-auto=update` recreates tables. Ensure the local `zhaobiao-mysql` container is running before `mvn test`.
- Production changes must not rely only on Hibernate auto-update. Update or add a MySQL migration under `sql/mysql8/`, and keep `docs/production-db-schema-change-list.md` plus relevant handoff/deployment docs current.
- Key system tables: `sys_user`, `sys_role`, `sys_permission`, `sys_menu`, `sys_user_role`, `sys_role_permission`, `sys_role_menu`, `sys_operation_log`.
- Historical table still present in code: `sys_user_audit_record`.
- Key business tables: `portal_member_user`, `portal_member_business_type_rel`, `biz_business_type`, `biz_tender`, `biz_tender_attachment`, `biz_file_storage`, `biz_news`.
- `portal_member_user` stores member lifecycle fields such as `expires_at`, `first_login_at`, and optional profile-file references `business_license_file_id` and `three_year_performance_file_id`.
- Anonymous portal visitors can call `/api/portal/tenders/latest` to see the latest 3 `PUBLISHED` tenders whose `publish_at` is not in the future, without business-type filtering.
- Portal tender detail text at `/api/portal/tenders/{id}` is public for published/current tenders and is not business-type filtered.
- Authenticated member portal list queries at `/api/portal/tenders` still return only published/current tenders whose business type is assigned to the member; the optional `businessTypeName` filter is a name filter inside the member's allowed business types.
- Portal news list/latest/detail endpoints only expose `PUBLISHED` news whose `publish_at` is not in the future.
- For environments that have already run `sql/mysql8/2026-05-23-complete-release.sql`, use `sql/mysql8/2026-05-23-news-publish-permission.sql` as the follow-up incremental script for the final news publish permission split.

## Security And Business Rules

- Responses use `ApiResponse`; expected business and validation errors usually return HTTP 200 with non-zero `code`. Frontends must check `code`, not only HTTP status.
- Authentication uses `Authorization: Bearer <token>`.
- JWT contains `userType`; admin and member users are loaded by different user-detail services.
- Public auth endpoints: `/api/auth/**`, `/api/portal/auth/captcha`, `/api/portal/auth/login`, `/api/portal/auth/register`, `/api/portal/auth/registration-status`. Backend admin register remains disabled; portal self-registration is controlled by the member registration setting.
- Other public backend endpoints include `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/`, `/index.html`, `/assets/**`, `/favicon.ico`, `/api/files/{fileId}/thumbnail`, `/api/portal/tenders/latest`, `/api/portal/tenders/{id}`, and `/api/portal/news/**`.
- Admin APIs are under `/api/admin/**` plus `/api/profile`.
- Portal tender list `/api/portal/tenders` requires `MEMBER`; public visitors must use `/api/portal/tenders/latest`.
- Portal tender attachment downloads require `MEMBER`; do not make `/api/portal/tenders/{id}/attachments/{attachmentId}/download` public.
- `DataInitializer` defines the seed set for permissions, menus, roles, initial business types, and initial `admin`; tests import it directly, and MySQL manual seeding is captured in `sql/mysql8/data-initializer.sql`.
- Admin roles currently seeded: `SUPER_ADMIN`, `SYSTEM_ADMIN`, `USER_AUDITOR`, `NORMAL_USER`.
- Seeded business types: `ENGINEERING`, `GOODS`, `SERVICE`.
- `SUPER_ADMIN` is the only role allowed to manage administrator accounts at `/api/admin/admin-users`.
- The initial `admin` user must not be disabled and must keep `SUPER_ADMIN`.
- Admin accounts must not receive `NORMAL_USER`; member/ordinary accounts must not receive administrator-only powers unless the business rule changes explicitly.
- Ordinary admins must not get the administrator-account management menu or APIs unless the business rule changes explicitly.
- Legacy `/api/admin/users` is intentionally disabled and returns business code `410`; do not reopen it accidentally.
- Admin tender create always persists `DRAFT`; tender publish/unpublish must use `PUT /api/admin/tenders/{id}/status` and requires `TENDER_PUBLISH_BUTTON`.
- Published tenders cannot be edited, deleted, or have attachments added/removed. Only a user with `TENDER_PUBLISH_BUTTON` may set them back to `DRAFT`.
- Admin news create always persists `DRAFT`; news publish/unpublish must use `PUT /api/admin/news/{id}/status` and requires `NEWS_PUBLISH_BUTTON`.
- Published news cannot be edited or deleted. Only a user with `NEWS_PUBLISH_BUTTON` may set them back to `DRAFT`.
- News categories are `PLATFORM_NOTICE`, `INDUSTRY_NEWS`, `SERVICE_GUIDE`, and `POLICY_REGULATION`; news statuses are `DRAFT` and `PUBLISHED`.
- Portal attachment download requires a valid member token, tender published/current, attachment ownership, member business-type access to the tender, and member `canDownloadFile=true`.
- Portal register uses `multipart/form-data` with member base fields, `captchaId`, `captchaCode`, `businessLicenseFile`, and `threeYearPerformanceFile`; when `portal.member.registration.enabled=false`, register returns business code `403` and creates no member; successful self-registration creates `DISABLED`, no business types, no `expiresAt`, and `canDownloadFile=false`.
- The public portal should call `GET /api/portal/auth/registration-status` and hide the register entry when `registrationEnabled=false`; backend enforcement remains `POST /api/portal/auth/register`.
- Admin member registration setting APIs live under `/api/admin/members/registration-setting`: `GET` requires `SYSTEM_MEMBER_USER`, `PUT` requires `MEMBER_REGISTRATION_SETTING_BUTTON`.
- Portal login requires `captchaId` and `captchaCode`; `DISABLED` members cannot log in until an admin adds business types, sets an expiry time, and enables them.
- Captcha images are served by `GET /api/portal/auth/captcha?scene=register|login&captchaId=<uuid>`; codes expire after 5 minutes, are one-time use, and compare case-insensitively.
- Admin member edit/status changes support only `ENABLED` and `DISABLED`; enabling a member requires at least one business type and an expiry time.
- Member login sets `first_login_at` only when it was empty and returns `profileCompletionRequired=true` for that first successful login.
- Admin member create/edit and member self profile update may set `business_license_file_id` and `three_year_performance_file_id`; these fields are optional and reuse `biz_file_storage`.
- When adding an admin capability, update controller authorization, `DataInitializer` permissions/menus/roles, frontend route/menu/button behavior, production SQL, docs, and tests together.
- In `prod`, `ProductionDatabaseSecurityValidator` blocks empty DB credentials, `root` DB user, and the default password `root`.

## File Storage

- Local storage writes under `.uploads/` by default; tests write under ignored local directories.
- Local file DB paths are relative paths like `YYYYMMDD/<uuid>.<ext>`.
- OSS storage is activated by `APP_FILE_TYPE=oss`.
- OSS requires bucket and endpoint. `APP_FILE_OSS_CREDENTIAL_MODE=access-key` requires access key id and secret; `APP_FILE_OSS_CREDENTIAL_MODE=ecs-ram-role` requires `APP_FILE_OSS_ROLE_NAME` and uses the ECS instance RAM role instead of long-lived AK/SK.
- OSS normalizes endpoints by adding `https://` when no scheme is present.
- OSS object keys use the configured prefix, defaulting to `zb/files/YYYYMMDD/<uuid>.<ext>`.
- Stored DB path is an object key or relative path, not a bucket name.
- Changing OSS bucket, region, endpoint, or key prefix may require migrating historical objects, not just changing env vars.
- Uploads are content-hash deduplicated through `biz_file_storage.content_hash`; preserve this behavior when changing attachment code.
- When DB save fails after writing a local/OSS object, current services try to clean up the newly written object.
- Member profile files reuse `biz_file_storage`; file cleanup must count both tender attachment references and member profile-file references before deleting storage records or physical objects.
- Public thumbnails are served by `GET /api/files/{fileId}/thumbnail`; frontends should render `thumbnailUrl` directly and fall back to a file icon when `thumbnailStatus` is `UNSUPPORTED` or `FAILED`.
- Admin file download is `GET /api/admin/files/{fileId}/download`; admin file inline view is `GET /api/admin/files/{fileId}/view`. Both return the complete file stream and require admin permissions, while preview UI and PDF paging are frontend responsibilities.
- Frontends must not construct local filesystem paths, OSS bucket URLs, or OSS object keys. Use file IDs and backend URLs so local, test, and production OSS storage remain interchangeable.

## Admin Frontend: `frontend/`

Commands:

```bash
cd frontend
npm install
npm run dev
npm run build:prod
npm run lint
```

Notes:

- `package.json` scripts also include `build:test`, `build:nomock`, `test:unit`, and SSH deploy scripts.
- Dev proxy defaults to `https://xiazhiyong.vip` unless `VUE_APP_PROXY_TARGET` is set.
- Production API base is `https://xiazhiyong.vip/api` from `frontend/src/setting.env.js`.
- Production public path is `/ztbgl/`.
- Production output is `frontend/dist`.
- Deploy target in scripts is `/usr/share/nginx/ztbgl`.
- The admin UI uses `frontend/src/plugins/request/index.js`; it treats `code` 0 or 200 as success and redirects on 10001/401.
- Current admin business pages live under `frontend/src/pages/sys`: administrator accounts, members, business types, tenders, menus, roles, and permissions.
- The admin UI needs a news-management page for `SYSTEM_NEWS` and button-level checks for `NEWS_*_BUTTON` and `TENDER_PUBLISH_BUTTON`.
- Admin file preview should happen inside the current page, such as a modal/drawer using `/api/admin/files/{fileId}/view`; do not open a new browser tab as the primary interaction.
- Use `docs/frontend-change-guide-2026-05-23.md` as the current handoff for news, tender publish status, file preview/download, member profile files, and portal news/tender changes.
- `frontend/.env` is tracked and currently only sets a public page title. Do not put secrets there.

## Public Portal: `front/`

Commands:

```bash
cd front
npm install
npm run dev
npm run build
```

Notes:

- Dev server port is `5174`.
- Dev proxy defaults to `https://xiazhiyong.vip` unless `VITE_PROXY_TARGET` is set.
- API base defaults to `/api`; set `VITE_API_BASE_URL` when needed.
- Production base is `/ztbfb/`.
- Production output is `front/dist`.
- Deploy target in scripts is `/usr/share/nginx/ztbfb`.
- Routes currently include `/` for login, `/list` for tender list, and `/detail/:id` for tender detail; add public news list/detail routes when implementing the 2026-05-23 handoff.
- Portal auth is stored in localStorage key `zb_portal_auth`.
- The `/list` page should call `/api/portal/tenders/latest` when no member token is present, and refresh to `/api/portal/tenders` after successful member login.
- Authenticated portal tender list filters by `businessTypeName` when the user selects a business-type category.
- Portal detail can be opened without login, but `canDownload=false` for guests.
- Portal downloads call `/api/portal/tenders/{tenderId}/attachments/{attachmentId}/download` with the member token.
- Portal news calls `/api/portal/news`, `/api/portal/news/latest`, and `/api/portal/news/{newsId}` without a member token.

## Deployment Notes

- Tracked frontend deploy scripts default to host `114.55.166.12` and user `root`.
- Nginx exposes the admin frontend at `/ztbgl/` and the public portal at `/ztbfb/`; `/` redirects to `/ztbfb/`.
- Captured Nginx root paths are `/usr/share/nginx/ztbgl` and `/usr/share/nginx/ztbfb`.
- Backend JAR name: `target/zhaobiao-admin.jar`.
- Known backend deploy directory from prior operations: `/opt/zhaobiao/app`.
- Runtime env file on the server is expected at `/opt/zhaobiao/app/app.env`.
- Backend file logs default to `/opt/zhaobiao/app/logs/zhaobiao-admin.log` under systemd because `APP_LOG_PATH` defaults to `./logs` and the service working directory is `/opt/zhaobiao/app`; archived file logs are retained for 60 days by default, and systemd journal remains available through `journalctl -u zhaobiao-admin`. Use `APP_LOG_LEVEL` for `com.zhaobiao.admin`, keep `APP_ROOT_LOG_LEVEL=INFO` unless diagnosing framework internals.
- `DEPLOY_LINUX.md` shows a generic `zhaobiao.service` unit. The real host has previously used `zhaobiao-admin.service`; always verify current units before restarting.
- Prefer a single `systemd`-managed backend process. Avoid leaving manual `nohup java -jar` processes behind.
- Prefer actual host checks over stale PID files:

```bash
systemctl list-units --type=service --all | grep -i zhaobiao
systemctl status zhaobiao-admin
ss -ltnp | grep ':8080'
curl -I http://127.0.0.1:8080/swagger-ui.html
curl -I http://127.0.0.1:8080/v3/api-docs
```

- On this host, `scp -O` has been more reliable for JAR transfers in prior operations.

## Secrets And Generated Files

- Do not commit or paste real passwords, tokens, OSS keys, JWT secrets, or database credentials.
- `server-config.md` is ignored by Git and may contain real operational credentials. Read it only when the user explicitly asks for production/server work, and never copy secrets into generated docs or chat output.
- Server `app.env` files must stay out of commits even if they are created locally for testing.
- Generated/local artifacts include `target/`, `.uploads/`, `.test-uploads/`, `frontend/node_modules/`, `frontend/dist/`, `front/node_modules/`, and `front/dist/`.
- Frontend package-specific `.gitignore` files already ignore their own `node_modules` and `dist` directories.

## Coding Conventions

- Keep backend code Java 8 compatible; do not introduce records, `var`, text blocks, or newer Java APIs.
- Keep transaction boundaries in services.
- Controllers should return `ApiResponse` and delegate business logic to services.
- Use `BusinessException` for expected business failures.
- Use Bean Validation annotations on DTOs for request validation.
- Use `@PreAuthorize` for endpoint authorization and keep permission codes aligned with `DataInitializer`.
- Keep operation logging through `@OperationLogRecord` for mutating admin actions where existing modules do so.
- Do not silently change production-facing defaults such as endpoint paths, role codes, permission codes, table names, public base paths, or deploy directories.
- When changing portal tender visibility or download logic, test member business-type assignments and `canDownloadFile`.
- When changing admin menus/roles/permissions, update both backend seed data and admin frontend navigation/buttons.

## Verification Expectations

- Backend behavior or schema changes: run `mvn test`.
- Security/permission changes: add or update integration tests under `src/test/java/com/zhaobiao/admin`.
- File upload/download changes: include duplicate-upload and download-permission coverage.
- Tender/news publish-permission changes: cover roles that can create/edit but cannot publish/unpublish, plus published records rejecting edit/delete/attachment mutation.
- Admin frontend changes: run `cd frontend && npm run build:prod`; run lint when touching shared UI code.
- Public portal changes: run `cd front && npm run build`.
- Deployment or production DB work: verify against the real running service/database, not only local files.
- Documentation-only changes normally do not require a full build, but still inspect the final diff for stale commands or secrets.

## Known Drift And Questions To Confirm

- `frontend/production.env` is tracked but empty; do not use it for secrets.
- `app.env` is mentioned in deployment docs but is not meant to live in the repo. If a local `app.env` appears, confirm whether it should be ignored or removed before committing.
- `DataInitializer` looks like startup seed logic, but current main app does not register it as a bean. Confirm the intended direction before changing initialization behavior.
- Portal public registration (`/api/portal/auth/register`) is enabled and creates `DISABLED` members; do not bypass the admin enablement step when changing onboarding.
- If a task says "the frontend", clarify whether it means admin console `frontend/` or public portal `front/`.
