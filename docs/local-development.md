# Local Development

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8.x
- Redis 6.x or newer
- Node.js 20.19+ or 22.12+ for the frontend

## Database

From the backend directory, run the SQL files in this order:

```powershell
Get-ChildItem .\sql\mysql\*.sql | Sort-Object Name | ForEach-Object {
  Get-Content $_.FullName | mysql -uroot -p
}
Get-Content .\sql\12_phase4_user_management.sql | mysql -uroot -p
Get-Content .\sql\13_phase5_evaluation_form.sql | mysql -uroot -p
Get-Content .\sql\15_phase7_school_management.sql | mysql -uroot -p
Get-Content .\sql\16_phase8_platform_management.sql | mysql -uroot -p
Get-Content .\sql\schema_check.sql | mysql -uroot -p
```

The first pass creates the schema and development seed data. Existing data is retained:
the incremental scripts only add missing columns/indexes and write their versions to
`sys_schema_migration`. Always take a `mysqldump` backup before an upgrade.

## Backend

Copy `.env.example` to `.env` and adjust credentials. Then start:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn -pl campus-admin -am spring-boot:run
```

Useful checks:

- `GET http://localhost:8080/api/health`
- `GET http://localhost:8080/api/health/db`
- `GET http://localhost:8080/api/health/redis`

The aggregate health endpoint returns HTTP 200 only when both MySQL and Redis are reachable;
otherwise it returns HTTP 503 and reports each component as `UP` or `DOWN`.

## Frontend

Copy the frontend `.env.example` to `.env.local`, then run:

```powershell
cd ..\campus-evaluation-fronted
npm install
npm run dev
```

The Vite development proxy targets the backend URL from `VITE_DEV_SERVER_TARGET`.
Production builds use `VITE_API_BASE_URL` for the API base URL.
