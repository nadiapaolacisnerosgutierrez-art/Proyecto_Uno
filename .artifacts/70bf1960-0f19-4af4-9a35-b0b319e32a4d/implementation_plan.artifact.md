# Implementation Plan - Persistent Friend Requests & Permanent Profile Picture

Implement persistent friend requests in SQLite and permanent profile picture storage in `filesDir` and SQLite (`garabato.db`).

## User Review Required

> [!IMPORTANT]
> - **Friend Requests (`amistad` table)**: Will create an `amistad` table (`idEmisor`, `idReceptor`, `estado` = `'pendiente'` or `'aceptada'`). Sending a request sets `'pendiente'`. Accepting sets `'aceptada'` making users mutual friends.
> - **Permanent Profile Picture**: Selected avatar image will be copied to `context.filesDir/avatar_usuario_${idUsuario}.jpg`, and its absolute path saved permanently in the `avatarUrl` column of the `usuario` table in `garabato.db`.

## Proposed Changes

### [Database Layer]
#### [MODIFY] [GarabatoDbHelper.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/data/GarabatoDbHelper.kt)
- Add `amistad` table creation in `onCreate` (`idEmisor`, `idReceptor`, `estado`).
- Add helper methods for sending, accepting, rejecting friend requests and fetching friends.

### [UI / Activity Layer]
#### [MODIFY] [AmigosActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/AmigosActivity.kt)
- Show real incoming friend requests with "Aceptar" and "Rechazar" buttons.
- Show "Enviar solicitud" when searching users, updating to "Solicitud enviada".
- Show mutual accepted friends in "Tus amigos".

#### [MODIFY] [PerfilActivity.kt](file:///C:/Users/nadia/AndroidStudioProjects/Practica1/app/src/main/java/com/jdl/practica1/PerfilActivity.kt)
- Load permanent avatar image from SQLite `avatarUrl`.
- Save picked gallery image locally to `filesDir` and persist path in `garabato.db`.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to verify successful compilation.
