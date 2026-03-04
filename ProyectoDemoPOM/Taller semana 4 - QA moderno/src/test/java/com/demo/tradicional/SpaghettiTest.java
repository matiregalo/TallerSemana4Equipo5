package com.demo.tradicional;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║          ESTILO TRADICIONAL — "SPAGHETTI TEST"                       ║
 * ║                                                                       ║
 * ║  ❌ PROBLEMAS de este enfoque:                                        ║
 * ║  1. TODO el código de Selenium vive dentro de @Test → difícil de     ║
 * ║     leer, mantener y reutilizar.                                      ║
 * ║  2. Si el id "task-input" cambia en el HTML, hay que buscar y         ║
 * ║     reemplazar en TODOS los tests manualmente.                        ║
 * ║  3. Cero abstracción: el test sabe cómo funciona la UI.               ║
 * ║  4. Duplicación garantizada al añadir más tests.                      ║
 * ║  5. Una sola clase de +100 líneas: el "código espagueti".             ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpaghettiTest {

    // ── FRÁGIL: el driver vive en el test sin ninguna capa de abstracción ──
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Apunta al binario local: no requiere conexión a internet
        // OPCIÓN 1: Driver local (Usar si no hay internet o falla el setup)
        System.setProperty("webdriver.edge.driver", "./msedgedriver.exe");

        // OPCIÓN 2: Automatizado (Descomentar si hay acceso a internet)
        // WebDriverManager.edgedriver().setup();

        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--start-maximized"); // ventana visible, sin headless
        driver = new EdgeDriver(opts);

        // ❌ FRÁGIL: la ruta del archivo está hardcodeada aquí mismo.
        //    Si el archivo se mueve, TODOS los tests fallan y hay que
        //    actualizar cada uno individualmente.
        String htmlPath = new File("index.html").getAbsolutePath();
        driver.get("file:///" + htmlPath.replace("\\", "/"));
    }

    @AfterEach
    void tearDown() {
        //if (driver != null) driver.quit();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  El único método @Test — TODA la lógica está mezclada aquí
    // ══════════════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("[TRADICIONAL] Agregar tareas, validar lista y eliminar")
    void escenarioCompletoSinPOM() throws InterruptedException {

        // ── PASO 1: Localizar y completar el input ────────────────────────
        // ❌ FRÁGIL: By.id("task-input") está escrito directamente en el test.
        //    Si el diseñador renombra el campo a "new-task", este test rompe.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement inputField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("task-input"))
        );
        inputField.sendKeys("Aprender POM");

        // ❌ FRÁGIL: By.id("add-btn") duplicado en cada test que necesite
        //    agregar una tarea. Cambiar el id → actualizar N tests.
        WebElement addButton = driver.findElement(By.id("add-btn"));
        addButton.click();

        // ── PASO 2: Agregar segunda tarea ─────────────────────────────────
        // ❌ FRÁGIL: repetición manual del mismo bloque de código.
        //    DRY violado: si la lógica de "agregar" cambia, hay que tocar
        //    cada bloque por separado.
        WebElement inputField2 = driver.findElement(By.id("task-input"));
        inputField2.sendKeys("Comprar leche");

        WebElement addButton2 = driver.findElement(By.id("add-btn"));
        addButton2.click();

        // ── PASO 3: Validar que existen 2 tareas ──────────────────────────
        // ❌ FRÁGIL: el selector CSS "#task-list li" está repetido en el
        //    método de test. Si la estructura HTML cambia a <ol> o a <div>,
        //    el test falla sin decirte qué page object actualizar.
        Thread.sleep(300); // ❌ sleep estático: lento y no confiable

        List<WebElement> tasks = driver.findElements(By.cssSelector("#task-list li"));
        assertEquals(2, tasks.size(),
            "Debe haber 2 tareas en la lista");

        // ── PASO 4: Eliminar la primera tarea ─────────────────────────────
        // ❌ FRÁGIL: si el botón cambia de clase "delete-btn" a "remove-btn",
        //    falla aquí Y en cada test que haga lo mismo.
        WebElement firstDeleteBtn = driver.findElement(By.cssSelector(".delete-btn"));
        firstDeleteBtn.click();

        // ── PASO 5: Validar que queda exactamente 1 tarea ─────────────────
        Thread.sleep(300); // ❌ otro sleep estático innecesario

        List<WebElement> tasksAfterDelete = driver.findElements(By.cssSelector("#task-list li"));
        assertEquals(1, tasksAfterDelete.size(),
            "Después de eliminar debe quedar 1 tarea");

        System.out.println("[TRADICIONAL] ✓ Test pasó, pero fíjate en cuánto");
        System.out.println("              código de Selenium vive dentro del @Test.");
        System.out.println("              Imagina tener 50 tests así...");
    }
}
