# Implementation Plan - Database & ViewModel Integration (Clean Architecture)

Connect the SQLite/Room database (`GarabatoDatabase` / `GarabatoDbHelper`) to the Clean Architecture repository and ViewModel, allowing screens like `InicioActivity` and `PerfilActivity` to fetch and display real stored data from the database.

## User Review Required

> [!IMPORTANT]
> - **ViewModel Factory / AppContainer**: Will provide a clean way to instantiate `GarabatoViewModel` with `GarabatoRepositoryImpl`.
> - **Real Data in UI**: `InicioActivity` and `PerfilActivity` will observe flows from the ViewModel to display real drawings, users, and counts.

## Proposed Changes

### [ViewModel & DI]
#### [MODIFY] [GarabatoViewModel.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/presentation/viewmodel/GarabatoViewModel.kt)
- Add StateFlows for drawings list (`dibujos`), users list (`usuarios`), and methods to load data from repository.

#### [MODIFY] [InicioActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/InicioActivity.kt)
- Connect to `GarabatoViewModel` to display real database records in the feed.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to verify compilation.
