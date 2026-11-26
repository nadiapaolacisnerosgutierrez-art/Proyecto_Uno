# Walkthrough - Essential Flows & Privacy Policy

Successfully connected drawing publication with user session management and profile refresh:
1. **LienzoActivity**: Retrieves the active `idUsuario` from `SharedPreferences` (with robust fallback to database query/creation), associates the new drawing precisely with that user ID when publishing, and redirects to `PerfilActivity`.
2. **PerfilActivity**: Implemented `onResume()` and `onCreate()` to reload the active user's profile info and fetch their published drawings from `GarabatoDbHelper`, ensuring newly published drawings appear immediately in the "Dibujos" tab with updated counters.

## Verification Results

### Automated Tests
- Ran `gradle_build("app:assembleDebug")`: **Success** (`Build finished successfully.`).
