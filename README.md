# Simple Shopping List 🛒

Lista de la compra **compartida y colaborativa** para Android, respaldada por **Firebase Realtime Database** (acceso vía REST con Retrofit). Interfaz moderna en **Jetpack Compose + Material 3**.

Cada persona que configure la app con la misma URL de base de datos verá y editará **la misma lista** en tiempo casi real (pull-to-refresh). Los artículos muestran quién los añadió (`owner`).

## Características

- ✅ Añadir artículos con diálogo rápido (FAB **+**).
- ☑️ Marcar/desmarcar: el texto se tacha y el artículo baja al bloque inferior.
- 🗑️ Eliminar deslizando hacia la izquierda (swipe-to-dismiss).
- ⌨️ El diálogo de alta abre el teclado con el foco en el campo de texto.
- 🔽 Pull-to-refresh para recargar desde el servidor.
- 🔧 Configuración en la propia app (endpoint, secret, usuario), persistida en el dispositivo.
- 📋 Ordenación fija:
  1. **No comprados** (arriba), del más nuevo al más antiguo (por `created`).
  2. **Comprados** (abajo), del marcado más reciente al más antiguo (por `updated`).
- 🎨 Material 3, tema claro/oscuro, colores dinámicos en Android 12+.

## Captura del modelo de datos

Cada artículo se guarda en Firebase como:

```json
{
  "item-1730721600000": {
    "created": "2026-11-04T15:00:00Z",
    "updated": "2026-11-04T16:30:00Z",
    "description": "Leche",
    "owner": "dani",
    "checked": false
  }
}
```

- `created` / `updated`: fecha de alta y último cambio (al desmarcar, `updated` vuelve a ser `created`).
- `owner`: usuario configurado en la app (quién añadió el artículo).
- `checked`: estado de compra.

## Requisitos

- Android 7.0 (API 24) o superior.
- Para compilar: Android Studio (o JDK 17+) con Android SDK 37.
- Un proyecto de Firebase (gratuito) — ver siguiente sección.

## Crear el proyecto y la base de datos en Firebase

1. **Crear proyecto**: ve a [console.firebase.google.com](https://console.firebase.google.com) → **Crear un proyecto** → dale un nombre (p. ej. `shopping-list`) → desactiva Analytics si quieres → Crear.

2. **Crear la base de datos**:
   - En el menú lateral: **Compilación (Build) → Realtime Database** → **Crear base de datos**.
   - Elige una ubicación (p. ej. `Bélgica (europe-west1)`).
   - **Importante**: la URL de tu base de datos será algo como `https://shopping-list-xxxx-default-rtdb.europe-west1.firebasedatabase.com` — la necesitarás luego. Cópiala de la cabecera de la pestaña **Datos**.

3. **Configurar las reglas de seguridad**: pestaña **Reglas**, y para empezar (modo test):

   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

   > ⚠️ Con estas reglas **cualquiera que conozca la URL puede leer y escribir**. Si quieres restricción básica por secreto, usa `?auth=` con un *database secret* (ver paso 4) y reglas como `{"rules": {".read": "auth != null", ".write": "auth != null"}}`.

4. **(Opcional) Obtener el secret** para el parámetro `?auth=`:
   - Consola → ⚙️ **Configuración del proyecto** → **Cuentas de servicio** → **Secretos de la base de datos** → mostrar y copiar.
   - Si usas reglas en modo test (paso 3), puedes dejar este campo vacío en la app.

5. **Crear la "lista" (nodo)**: no hay que crear nada manualmente — se crea solo con la primera escritura. La app usará el nodo que indiques en el endpoint. Se recomienda un identificador difícil de adivinar (p. ej. un UUID) para que tu lista no sea pública por enumeración:

   ```
   https://TU-INSTANCIA.firebasedatabase.app/TU_LISTA
   ```

   Puedes probarlo con `curl` antes de instalar nada:

   ```bash
   curl -X PUT \
     "https://TU-INSTANCIA.firebasedatabase.app/TU-LISTA.json?auth=TU_SECRET" \
     -H "Content-Type: application/json" \
     -d '{"item-1": {"created":"2026-11-04T10:00:00Z","updated":"2026-11-04T10:00:00Z","description":"Leche","owner":"dani","checked":false}}'
   ```

## Instalación

### Desde el código fuente

```bash
git clone <este-repositorio>
cd shopping-list
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk   # o copia el APK al móvil
```

En Android Studio: abre el proyecto, selecciona un dispositivo y pulsa ▶️ **Run**.

### Configurar la app (primer arranque)

Al abrirla por primera vez aparece el diálogo de **Configuración** (también accesible con el icono ⚙️ de la barra superior):

| Campo | Valor | Ejemplo |
|---|---|---|
| **Firebase endpoint** | URL de tu base de datos + nodo de la lista | `https://shopping-list-xxxx-default-rtdb.europe-west1.firebasedatabase.com/TU_LISTA` |
| **API key / secret** | Database secret (o vacío si tus reglas son modo test) | `AbC123...` |
| **Username** | Tu nombre; se guardará como `owner` en tus artículos | `dani` |

Pulsa **Guardar**, haz pull-to-refresh ¡y a comprar!

## Arquitectura

```
app/src/main/java/org/danigarcia/apps/simple_shopping_list/
├── MainActivity.kt                  # Entry point (Compose)
├── MainViewModel.kt                 # Estado (StateFlow<UiState>) + lógica de negocio
├── model/ShoppingItem.kt            # Modelo: created/updated/description/owner/checked
├── data/
│   ├── SettingsRepository.kt        # DataStore: endpoint, secret, username (persistente)
│   └── ShoppingListRepository.kt    # GET/PUT del árbol JSON + regla de ordenación
├── network/FirebaseApi.kt           # Retrofit con @Url dinámico (REST de Firebase)
└── ui/
    ├── Theme.kt                     # Material 3, paleta verde, dynamic color
    ├── MainScreen.kt                # Lista, swipe-to-dismiss, pull-to-refresh, FAB
    └── Dialogs.kt                   # Añadir artículo + Configuración
```

### Flujos de negocio

Todas las operaciones siguen el patrón **GET → modificar localmente → PUT** del árbol completo:

| Acción | Flujo |
|---|---|
| Añadir (FAB) | `GET` → insertar `{created: ahora, updated: ahora, owner: usuario, checked: false}` → `PUT` |
| Marcar | `GET` → `checked: true`, `updated: ahora` → `PUT` |
| Desmarcar | `GET` → `checked: false`, `updated = created` → `PUT` |
| Eliminar (swipe) | `GET` → filtrar el elemento → `PUT` |
| Refrescar | `GET` |

### Dependencias

Se gestionan con version catalog:

- `gradle/libs.versions.toml` — versiones (Compose BOM, Material 3, Lifecycle, DataStore, Retrofit + Gson).
- `app/build.gradle.kts` — bloque `dependencies { }`; `buildFeatures { compose = true }` y plugin `org.jetbrains.kotlin.plugin.compose`.

## Licencia

Uso libre para fines personales y educativos.
