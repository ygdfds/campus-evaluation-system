# Database Scripts

This directory is the backend-owned database entry point.

## Run Order

For a new local database:

1. Run `mysql/*.sql` in lexical order from `00_create_database.sql` through `11_stats.sql`.
2. Run `99_init_data.sql` for development seed data.
3. Run incremental migrations in this directory in numeric order: `12_phase4_user_management.sql`, `13_phase5_evaluation_form.sql`.

For an existing database:

1. Back up the database first.
2. Run only missing incremental migrations in numeric order.
3. The current incremental scripts are idempotent and record completion in `sys_schema_migration`.

`14_phase6_evaluation_submission.sql` is intentionally not part of this phase. It belongs to the later evaluation-closure workstream.
