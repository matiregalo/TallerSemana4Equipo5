# Taller Semana 4 — QA Moderno: POM vs Tradicional

Proyecto educativo que compara el patrón **Page Object Model (POM)** contra el estilo **Tradicional (Spaghetti Test)** usando Selenium 4, JUnit 5 y Gradle.

---

## Requisitos previos

### 1. Java JDK 17 o superior

El proyecto está configurado con `sourceCompatibility = JavaVersion.VERSION_17`. Cualquier JDK ≥ 17 funciona (el entorno de desarrollo usa JDK 25).

**Descargar:** https://www.oracle.com/java/technologies/downloads/

Verificar instalación:
```bash
java -version
# java version "17.x.x" o superior
```

---

### 2. Gradle 7.x o superior (o usar el Wrapper incluido)

> **Recomendado:** usar el Gradle Wrapper (`gradlew`) incluido en el proyecto. No requiere instalar Gradle globalmente.

Si se quiere instalar Gradle globalmente (el entorno de desarrollo usa Gradle 9.3.1):

**Descargar:** https://gradle.org/releases/

Verificar instalación:
```bash
gradle --version
```

#### Agregar Gradle al PATH (Windows)

1. Descomprimir Gradle en una ruta sin espacios, por ejemplo `C:\gradle\gradle-9.3.1`
2. Abrir **Inicio → Editar las variables de entorno del sistema**
3. En **Variables del sistema**, seleccionar `Path` → **Editar**
4. Agregar una nueva entrada: `C:\gradle\gradle-9.3.1\bin`
5. Aceptar y **cerrar y reabrir la terminal** para que tome efecto

Verificar:
```bash
gradle --version
```

#### Agregar Java al PATH (Windows)

Si `java -version` no reconoce el comando:

1. Ir a **Variables del sistema** → `Path` → **Editar**
2. Agregar: `C:\Program Files\Java\jdk-17\bin` *(ajustar según la ruta real de instalación)*
3. También crear/verificar la variable `JAVA_HOME`:
   - Nombre: `JAVA_HOME`
   - Valor: `C:\Program Files\Java\jdk-17` *(sin `\bin`)*
4. Cerrar y reabrir la terminal

---

### 3. Microsoft Edge

El proyecto usa **Microsoft Edge** como navegador de prueba.

**Verificar versión instalada:**
- Abrir Edge → `edge://version/` en la barra de direcciones
- O por PowerShell:
```powershell
(Get-Item "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe").VersionInfo.ProductVersion
```

El entorno de desarrollo usa **Edge 145.0.3800.82**.

---

### 4. msedgedriver.exe (driver local)

El proyecto **no usa internet** para gestionar el driver; apunta a un binario local ubicado en la raíz del proyecto.

#### Descargar el driver correcto

La versión de `msedgedriver.exe` **debe coincidir exactamente** con la versión de Edge instalada.

1. Ir a: https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
2. Descargar el driver que corresponda a tu versión de Edge (ej. `145.x.x.x`)
3. Extraer el archivo y copiar `msedgedriver.exe` en la **raíz del proyecto** (al lado de `build.gradle`)

Estructura esperada:
```
Taller semana 4 - QA moderno/
├── build.gradle
├── gradlew
├── gradlew.bat
├── index.html
├── msedgedriver.exe   ← aquí
└── src/
```

Verificar que el driver es compatible:
```bash
.\msedgedriver.exe --version
# MSEdgeDriver 145.0.3800.82 (...)
```

> ⚠️ Si la versión de Edge no coincide con la del driver, los tests fallarán con un error de sesión.

---

## Estructura del proyecto

```
src/
└── test/
    └── java/
        └── com/
            └── demo/
                ├── pom/
                │   ├── pages/
                │   │   ├── BasePage.java          ← clase base con utilidades Selenium
                │   │   └── TodoPage.java          ← Page Object de la app
                │   └── tests/
                │       └── OptimizedTodoTest.java ← test limpio usando POM
                └── tradicional/
                    └── SpaghettiTest.java         ← test tradicional sin abstracción
```

| Archivo | Descripción |
|---|---|
| `index.html` | Aplicación web Todo List usada como SUT (System Under Test) |
| `build.gradle` | Configuración de dependencias y tareas Gradle |
| `msedgedriver.exe` | Driver local de Edge para Selenium |
| `BasePage.java` | Clase base con waits explícitos reutilizables |
| `TodoPage.java` | Page Object que encapsula toda interacción con la UI |
| `OptimizedTodoTest.java` | Test legible que solo habla con el Page Object |
| `SpaghettiTest.java` | Test tradicional con Selenium mezclado en el `@Test` |

---

## Dependencias (build.gradle)

| Librería | Versión | Propósito |
|---|---|---|
| `selenium-java` | 4.27.0 | Automatización del navegador |
| `webdrivermanager` | 5.9.2 | Gestión automática de drivers *(requiere internet)* |
| `junit-jupiter` | 5.11.4 | Motor de pruebas |
| `junit-platform-launcher` | 1.11.4 | Launcher de JUnit 5 para Gradle |

> **Nota sobre WebDriverManager:** la dependencia está declarada en `build.gradle` pero los tests usan el driver local (`msedgedriver.exe`) mediante `System.setProperty`, ya que el entorno no tiene acceso a internet. Si hay conexión disponible, se puede reemplazar `System.setProperty` por `WebDriverManager.edgedriver().setup()`.

---

## Ejecutar los tests

Desde la raíz del proyecto, usando el Gradle Wrapper:

**Windows (PowerShell o CMD):**
```bash
.\gradlew test
```

**Linux / macOS:**
```bash
./gradlew test
```

**Ejecutar solo un test específico:**
```bash
.\gradlew test --tests "com.demo.pom.tests.OptimizedTodoTest"
.\gradlew test --tests "com.demo.tradicional.SpaghettiTest"
```

**Forzar re-ejecución aunque no haya cambios:**
```bash
.\gradlew cleanTest test
```

---

## Ver el reporte HTML

Después de ejecutar los tests, Gradle genera un reporte detallado:

```
build/reports/tests/test/index.html
```

Abrir desde PowerShell:
```powershell
Start-Process "build\reports\tests\test\index.html"
```

---

## Solución de problemas

### `java` no se reconoce como comando
→ Agregar `%JAVA_HOME%\bin` al PATH del sistema y crear la variable `JAVA_HOME` (ver sección 1).

### `.\gradlew` no se reconoce o da error de permisos
→ En PowerShell ejecutar primero:
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### `SessionNotCreatedException` al iniciar el driver
→ La versión de `msedgedriver.exe` no coincide con la versión de Edge instalada. Descargar el driver correcto desde https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/

### `msedgedriver.exe` no encontrado
→ Asegurarse de que `msedgedriver.exe` está en la **raíz del proyecto**, no en una subcarpeta. El path configurado es `"./msedgedriver.exe"` relativo al directorio de trabajo de Gradle (la raíz del proyecto).

### Tests pasan pero Edge se abre y cierra muy rápido
→ El `@AfterEach tearDown()` está comentado intencionalmente para visualizar el resultado. Descomentar `driver.quit()` en `tearDown()` para cerrar el navegador automáticamente.

### `WebDriverManagerException: UnknownHostException`
→ No hay conexión a internet. Usar el driver local con `System.setProperty("webdriver.edge.driver", "./msedgedriver.exe")` en lugar de `WebDriverManager.edgedriver().setup()`.

---

## Conceptos clave del taller

### Page Object Model (POM)
Patrón de diseño que separa la lógica de interacción con la UI (localizadores, waits, acciones) de la lógica del test. Beneficios:
- Los tests hablan en lenguaje de negocio, no de Selenium
- Un solo punto de actualización si la UI cambia
- Reutilización de código entre múltiples tests

### Test Tradicional ("Spaghetti")
Todo el código de Selenium vive dentro del método `@Test`. Problemático porque:
- Alta duplicación al agregar más tests
- Difícil de mantener si cambia la UI
- El test mezcla el "qué" con el "cómo"

---

## Tecnologías

- **Java 17+**
- **Gradle 7+** (Wrapper incluido: 9.3.1)
- **Selenium WebDriver 4.27.0**
- **JUnit 5.11.4**
- **Microsoft Edge 145+**
- **msedgedriver 145+**
