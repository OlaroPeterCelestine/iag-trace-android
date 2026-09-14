# Agent notes

Native Android Traceability. Keep RBAC in `core/` aligned with the web farmer-traceability desk (`visibleRoutes`, portal scoping).

- UI: `app/` (Jetpack Compose)
- Core: `core/` (roles, catalog, store — JVM, no Android SDK)
- Tests: `./gradlew :core:test` (must stay green in CI before `assembleDebug`)
