package com.demo.pom.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║                        BASE PAGE — POM                               ║
 * ║                                                                       ║
 * ║  ✅ VENTAJAS de centralizar aquí:                                     ║
 * ║  1. El WebDriver se inyecta UNA sola vez y lo heredan todas las       ║
 * ║     páginas. Los tests nunca tocan el driver directamente.            ║
 * ║  2. Los métodos de espera (waitFor*) están en un único lugar.         ║
 * ║     Si Selenium cambia su API de esperas, solo actualizas aquí.       ║
 * ║  3. Funcionalidades transversales (scroll, screenshot, JS executor)   ║
 * ║     se añaden aquí y quedan disponibles para todas las páginas.       ║
 * ║  4. Principio DRY aplicado a nivel de infraestructura de pruebas.     ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public abstract class BasePage {

    // ── WebDriver protegido: accesible por las subclases, oculto a los tests ──
    protected final WebDriver driver;

    // ── Espera explícita centralizada: configurable en un solo lugar ──────────
    private final WebDriverWait wait;

    /**
     * Constructor que inicializa el driver y delega en PageFactory para
     * resolver los @FindBy de la subclase.
     *
     * @param driver instancia de WebDriver proveniente de la clase de test
     */
    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // ✅ Timeout centralizado: cambiar "5" afecta TODAS las páginas.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // PageFactory escanea los @FindBy de la subclase y los inicializa
        // como proxies lazy → no hace la búsqueda hasta que se usan.
        PageFactory.initElements(driver, this);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos de espera reutilizables — disponibles para toda la jerarquía
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Espera hasta que un elemento sea visible en el DOM.
     *
     * ✅ ROBUSTO: usa espera explícita en lugar de Thread.sleep().
     *    Si el elemento aparece en 200 ms, el test no espera 3 segundos.
     *
     * @param element WebElement a esperar
     * @return el mismo elemento una vez visible
     */
    protected WebElement waitForVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Espera hasta que un elemento sea clickeable (visible + habilitado).
     *
     * @param element WebElement a esperar
     * @return el mismo elemento una vez clickeable
     */
    protected WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }
}
