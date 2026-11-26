# Implementation Plan - Real User Session, Profile DB Integration, & Amigos Interactivity

1. **Clean Test Data & Real User (`LoginActivity.kt` + `GarabatoDbHelper`)**:
   - Remove automatic dummy user insertion (`Ana García`, `Carlos Ruiz`, `Sofía López`) from `GarabatoDbHelper.onCreate`.
   - In `LoginActivity`, when registering or logging in, save the user into the `usuario` table of `garabato.db` and store their ID, name, and email in `SharedPreferences` (`garabato_user_prefs`).
   - Add a Privacy Policy button in `LoginActivity` opening a scrollable `AlertDialog`.
2. **Real User Profile (`PerfilActivity.kt`)**:
   - Read active user name, email, and drawing count from `SharedPreferences` and `GarabatoDbHelper`.
   - Display real drawings created by the active user in the "Dibujos" tab.
   - Add scrollable Privacy Policy dialog.
3. **Amigos Interactivity (`AmigosActivity.kt`)**:
   - Make friend request accept/reject and friend action buttons fully interactive (updating UI state dynamically).

## User Review Required

> [!IMPORTANT]
> - `GarabatoDbHelper.onCreate` will no longer insert hardcoded sample users automatically; users are created dynamically through `LoginActivity`.
> - Active user session is persisted via `SharedPreferences`.

## Proposed Changes

### [Files to Modify]
1. **[GarabatoDbHelper.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/data/GarabatoDbHelper.kt)**: Remove hardcoded sample test user insertion from `onCreate`.
2. **[LoginActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/LoginActivity.kt)**: Implement saving real user to database + SharedPreferences session, plus Privacy Policy button.
3. **[PerfilActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/PerfilActivity.kt)**: Load real active user profile from SharedPreferences and real drawings from database.
4. **[AmigosActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/AmigosActivity.kt)**: Make friend requests interactive.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to verify successful compilation.
