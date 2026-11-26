# Contexto del Proyecto: Garabato

## Descripción General
**Garabato** es una aplicación móvil desarrollada en Android Studio para una comunidad de dibujo y compartición de arte. Permite a los usuarios visualizar publicaciones de la comunidad (feed de inicio) y acceder a un lienzo de dibujo interactivo.

## Stack Tecnológico
- **Lenguaje:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Persistencia Local:** Room Database (Etapa 1 implementada: Entidades, DAOs y Base de Datos Singleton)
- **SDKs:** minSdk 24, targetSdk 36
- **Arquitectura:** Multi-actividad con navegación basada en `Intent`

---

## Estructura y Componentes Clave

### 1. Interfaz de Usuario (UI con Jetpack Compose)
- **`InicioActivity` (Pantalla 1 - Inicio / Feed):**
  - Barra superior (*TopAppBar*) con logo y botones de búsqueda/notificaciones.
  - Fila de historias con scroll horizontal (avatares circulares con iniciales).
  - Chips de filtro (*Para ti, Amigos, Retos, Tendencias*).
  - Tarjeta de publicación (*Card*) con cabecera de autor, un dibujo de un gato renderizado mediante `Canvas`, contadores de likes/comentarios y botón *"Redibujar"*.
  - Barra de navegación inferior (*NavigationBar*) con botón flotante central `+` para abrir el lienzo.
- **`LienzoActivity` (Pantalla 2 - Lienzo de Dibujo):**
  - Barra superior con botón `X` (`finish()`), título *"Lienzo sin título"*, botones de deshacer/rehacer y botón *"Publicar"*.
  - Área de lienzo con un dibujo de ejemplo (casa, sol y olas) renderizado con `Canvas`.
  - Barra de herramientas con íconos (lápiz, pincel, borrador, cubeta, formas).
  - Fila horizontal de selección de colores y un `Slider` para ajustar el grosor del trazo.
- **Navegación:**
  - Uso de `Intent` explícito desde `InicioActivity` hacia `LienzoActivity` al presionar el botón `+` o *"Redibujar"*.
  - Cierre de `LienzoActivity` mediante `finish()` al presionar la `X`.

### 2. Sistema de Diseño y Temas
- **`AppTheme` / `GarabatoTheme`**: Tema personalizado en el paquete `com.jdl.practica1.ui.theme`.
- **`Color.kt`, `Theme.kt`, `Type.kt`**: Esquemas de color adaptativos (Material 3 light/dark/contrast) y tipografía personalizada (`AppTypography`).
- **Uso estricto**: Todos los colores se obtienen de `MaterialTheme.colorScheme` y todos los textos provienen de `res/values/strings.xml` mediante `stringResource`.

### 3. Capa de Datos (Room Database - Etapa 1)
- **Entidades (`com.jdl.practica1.data.entity`)**:
  - `UsuarioEntity`: Tabla `usuario` (ID, nombre, correo, avatar).
  - `DibujoEntity`: Tabla `dibujo` (ID, título, fecha, relación FK con `usuario`).
  - `ComentarioEntity`: Tabla `comentario` (ID, contenido, fecha, relaciones FK con `usuario` y `dibujo`).
  - `MeGustaEntity`: Tabla `me_gusta` (Clave primaria compuesta `idUsuarioFK` + `idDibujoFK`, fecha).
- **DAOs (`com.jdl.practica1.data.dao`)**:
  - `UsuarioDao`, `DibujoDao`, `ComentarioDao`, `MeGustaDao`: Operaciones CRUD completas utilizando corrutinas (`suspend`) y flujos reactivos (`Flow`).
- **Base de Datos (`com.jdl.practica1.data.GarabatoDatabase`)**:
  - Clase abstracta `RoomDatabase` implementada con el patrón **Singleton** (`getInstance`), configurada para persistencia local.

---

## Restricciones Académicas Respetadas
- Cero colores quemados tipo `Color(0xFF...)` en la UI.
- Cero textos fijos en el código (todo en `strings.xml`).
- Comentarios limpios y estructura modular adecuada para explicación en clase.
