package com.demo.pom.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║                       TODO PAGE — POM                                ║
 * ║                                                                       ║
 * ║  ✅ VENTAJAS del Page Object:                                         ║
 * ║  1. Los localizadores (@FindBy) están en UN solo lugar.               ║
 * ║     Si "task-input" cambia a "task-field", SE ACTUALIZA AQUÍ          ║
 * ║     y todos los tests que usen esta clase se arreglan solos.          ║
 * ║  2. Los métodos (agregarTarea, eliminarTarea) encapsulan los          ║
 * ║     pasos de la UI. Los tests NO saben nada de Selenium.              ║
 * ║  3. El nombre de los métodos documenta la intención del test:         ║
 * ║     page.agregarTarea("X") es autoexplicativo.                        ║
 * ║  4. Fácil de extender: añadir "completarTarea()" aquí y está          ║
 * ║     disponible para todos los tests sin duplicar código.              ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class TodoPage extends BasePage {

    // ══════════════════════════════════════════════════════════════════════════
    //  Localizadores — ÚNICA fuente de verdad para los selectores
    //
    //  ✅ @FindBy declara los localizadores de forma declarativa.
    //     PageFactory (en BasePage) inicializa estos WebElements como proxies.
    //     Si el HTML cambia, SOLO se actualiza este fichero.
    // ══════════════════════════════════════════════════════════════════════════

    /** Campo de texto donde se escribe la nueva tarea — id="task-input" */
    @FindBy(id = "task-input")
    private WebElement taskInput;

    /** Botón que agrega la tarea a la lista — id="add-btn" */
    @FindBy(id = "add-btn")
    private WebElement addButton;

    /**
     * Lista de todos los ítems {@code <li>} dentro de {@code #task-list}.
     * PageFactory resuelve esto como una lista lazy: se refresca en cada acceso,
     * por lo que siempre refleja el DOM actual después de agregar/eliminar.
     */
    @FindBy(css = "#task-list li")
    private List<WebElement> taskItems;

    /**
     * Todos los botones de eliminar (.delete-btn) en el DOM.
     * Al igual que taskItems, es una lista live.
     */
    @FindBy(css = ".delete-btn")
    private List<WebElement> deleteButtons;

    // ══════════════════════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @param driver instancia de WebDriver; BasePage se encarga de
     *               inicializar PageFactory y la espera explícita.
     */
    public TodoPage(WebDriver driver) {
        super(driver);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos de acción — La "API" de la página para los tests
    //
    //  ✅ Los tests llaman a ESTOS métodos, nunca a findElement ni a By.
    //     Esto significa que los tests son independientes de la implementación
    //     de la UI y son mucho más fáciles de leer y mantener.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Escribe {@code text} en el campo de tarea y pulsa "Agregar".
     *
     * ✅ ROBUSTO: usa waitForClickable (espera explícita heredada de BasePage)
     *    en lugar de un Thread.sleep() estático.
     *
     * @param text texto de la tarea a agregar
     */
    public void agregarTarea(String text) {
        waitForVisible(taskInput).clear();
        taskInput.sendKeys(text);
        waitForClickable(addButton).click();
    }

    /**
     * Elimina la tarea en la posición {@code index} (base 0).
     *
     * ✅ EXPRESIVO: el test dice "eliminar tarea 0" sin saber que internamente
     *    existe un selector ".delete-btn" ni cómo funciona el botón.
     *
     * @param index posición (0-based) del botón de eliminar a pulsar
     */
    public void eliminarTarea(int index) {
        waitForClickable(deleteButtons.get(index)).click();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos de consulta — Permiten hacer assertions sin acceder al DOM
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve el número actual de tareas en la lista.
     *
     * ✅ El test escribe: assertEquals(2, page.contarTareas())
     *    Legible como una oración en español.
     *
     * @return cantidad de elementos {@code <li>} en #task-list
     */
    public int contarTareas() {
        return taskItems.size();
    }
}
