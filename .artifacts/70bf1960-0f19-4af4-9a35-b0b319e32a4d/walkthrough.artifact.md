# Walkthrough - Persistent Friend Requests & Permanent Profile Picture

Successfully implemented persistent friendship requests in SQLite (`amistad` table) and permanent profile picture saving in `filesDir` and SQLite (`avatarUrl`), without modifying AndroidManifest.xml or build.gradle.kts.

## Changes

### [Database & Social Layer]
- **[GarabatoDbHelper.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/data/GarabatoDbHelper.kt)**: Created `amistad` table (`idEmisor`, `idReceptor`, `estado`), updated database version to 3, and implemented robust helper methods (`enviarSolicitudAmistad`, `obtenerSolicitudesPendientes`, `aceptarSolicitud`, `rechazarSolicitud`, `obtenerAmigosAceptados`, `obtenerEstadoAmistad`).
- **[AmigosActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/AmigosActivity.kt)**: Connected real friend requests section with Accept and Reject buttons, dynamic "Enviar solicitud" / "Solicitud enviada" states, and mutual friends list.
- **[PerfilActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/PerfilActivity.kt)**: Profile avatar picker copies selected image permanently to `filesDir/avatar_usuario_${userId}.jpg` and saves the absolute path into `avatarUrl` in SQLite, ensuring it is permanently loaded and never lost.

## Verification Results

### Automated Tests
- Ran `gradle_build("app:assembleDebug")`: **Success** (`Build finished successfully.`).
