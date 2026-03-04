package com.demo.pom.tests;

import com.demo.pom.pages.TodoPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║         ESTILO PAGE OBJECT MODEL — TEST OPTIMIZADO                   ║
 * ║                                                                       ║
 * ║  ✅ VENTAJAS de este enfoque:                                         ║
 * ║  1. El test NO importa ni usa ninguna clase de Selenium               ║
 * ║     (WebElement, By, ExpectedConditions). Solo habla con TodoPage.    ║
 * ║  2. Cada línea del test describe QUÉ hace la prueba, no CÓMO.        ║
 * ║  3. Si la UI cambia, SOLO se actualiza TodoPage; este test no         ║
 * ║     se toca.                                                          ║
 * ║  4. El test es tan legible que sirve como documentación viva          ║
 * ║     del comportamiento esperado de la aplicación.                     ║
 * ║  5. Puedes añadir 50 tests más reutilizando los métodos de            ║
 * ║     TodoPage sin duplicar ni una línea de Selenium.                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OptimizedTodoTest {

    // ── El test solo conoce el Page Object, no el WebDriver directamente ──────
    // (el driver se crea aquí únicamente para poder pasarlo al page object;
    //  idealmente en un proyecto real estaría en una fábrica o fixture base)
    private WebDriver driver;
    private TodoPage   todoPage;

    @BeforeEach
    void setUp() {
        // ✅ LIMPIO: la configuración del driver está aislada en setUp().
        // OPCIÓN 1: Driver local (Usar si no hay internet o falla el setup)
        System.setProperty("webdriver.edge.driver", "./msedgedriver.exe");

        // OPCIÓN 2: Automatizado (Descomentar si hay acceso a internet)
        // WebDriverManager.edgedriver().setup();

        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--start-maximized"); // ventana visible, sin headless
        driver = new EdgeDriver(opts);

        // ✅ La ruta del HTML también podría venir de una variable de entorno
        //    o de un archivo de configuración. Aquí es relativa al proyecto.
        String htmlPath = new File("index.html").getAbsolutePath();
        driver.get("file:///" + htmlPath.replace("\\", "/"));

        // ✅ El Page Object se inicializa UNA VEZ; a partir de aquí
        //    el test NO necesita saber nada de Selenium.
        todoPage = new TodoPage(driver);
    }

    @AfterEach
    void tearDown() {
        //if (driver != null) driver.quit();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Escenario de prueba — Limpio, expresivo, sin código de Selenium
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("[POM] Agregar tareas, validar lista y eliminar")
    void escenarioCompleto() throws InterruptedException {

        // ── PASO 1 & 2: Agregar dos tareas ───────────────────────────────────
        // ✅ LEGIBLE: lee como prosa. No hay By.id, no hay findElement.
        todoPage.agregarTarea("Aprender POM");
        todoPage.agregarTarea("Comprar leche");

        // ── PASO 3: Validar que hay exactamente 2 tareas ─────────────────────
        // ✅ La assertion habla del NEGOCIO, no de la UI.
        assertEquals(2, todoPage.contarTareas(),
            "Deben existir 2 tareas después de agregarlas");

        Thread.sleep(2000); // Para que se vean las 2 tareas antes de eliminar una

        // ── PASO 4: Eliminar la primera tarea (índice 0) ─────────────────────
        todoPage.eliminarTarea(0);
        

        // ── PASO 5: Validar que queda exactamente 1 tarea ────────────────────
        assertEquals(1, todoPage.contarTareas(),
            "Debe quedar 1 tarea después de eliminar la primera");

        System.out.println("[POM] ✓ Test pasó.");
        System.out.println("      Observa que NINGUNA línea del test menciona");
        System.out.println("      'task-input', 'add-btn' ni 'delete-btn'.");
        System.out.println("      Si el HTML cambia, solo actualizamos TodoPage.");
    }
}
