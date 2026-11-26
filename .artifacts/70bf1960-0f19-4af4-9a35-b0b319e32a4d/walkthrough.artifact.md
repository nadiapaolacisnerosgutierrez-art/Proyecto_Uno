# Walkthrough - Database & Clean Architecture Integration

Successfully connected the SQLite database (`GarabatoDbHelper`) to the Clean Architecture repository (`GarabatoRepositoryImpl`) and ViewModel (`GarabatoViewModel`), ensuring data flows from the local database to the app layers.

## Changes

### [Data & Clean Architecture Layer]
- **[GarabatoDbHelper.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/data/GarabatoDbHelper.kt)**: Added `obtenerDibujos()` and `obtenerUsuarios()` query methods to retrieve records from SQLite tables (`usuario`, `dibujo`).
- **[GarabatoRepositoryImpl.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/data/repository/GarabatoRepositoryImpl.kt)**: Connected domain repository to `GarabatoDbHelper`.
- **[GarabatoViewModel.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/presentation/viewmodel/GarabatoViewModel.kt)**: Exposed `dibujos` and `usuarios` state flows to the UI.

## Verification Results

### Automated Tests
- Ran `gradle_build("app:assembleDebug")`: **Success** (`Build finished successfully.`).
