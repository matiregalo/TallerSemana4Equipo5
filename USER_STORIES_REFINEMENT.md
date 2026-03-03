
---

## 1. Introducción

Este informe presenta el análisis de calidad realizado a las historias de usuario relacionadas con la **Gestión de Prioridad** del proyecto **SistemaTickets**. El diagnóstico se basa en los criterios **INVEST** (Independiente, Negociable, Valiosa, Estimable, Pequeña y Testeable).

---

## 2. Diagnóstico y Refinamiento

### HU-1.1: Asignar prioridad a un ticket

**Diagnóstico General:** La historia es valiosa para el negocio porque reemplaza la asignación aleatoria por una deliberada. Sin embargo, carece de un campo de "justificación" que la HU-1.3 requiere posteriormente para ser visualizada.

| Atributo | Versión Original | Versión Refinada por GEMA A | Diferencias Detectadas |
| --- | --- | --- | --- |
| **Descripción** | Como administrador quiero cambiar la prioridad de un ticket en estado Open O In-Progress para reflejar su urgencia real. | Como **Administrador**, quiero asignar una prioridad y **registrar una justificación** a un ticket en estado `OPEN` o `IN_PROGRESS`, para que el equipo entienda el motivo de la urgencia. | La GEMA añadió la obligatoriedad de registrar una **justificación**, alineándola con la necesidad de trazabilidad de la HU-1.3. |
| **Criterios de Aceptación** | Scenario: Administrador asigna prioridad en ticket Open. Given un ticket en estado "Open"... When selecciona la prioridad "High"... Then la prioridad se actualiza. | Escenario: Cambio de prioridad con motivo. **Given** un ticket en estado "Open", **When** el Admin selecciona "High" e **ingresa un motivo de > 10 caracteres**, **Then** el sistema actualiza la prioridad y el motivo. | Se incluyó una restricción de longitud mínima para el motivo y se aseguró la persistencia de ambos datos en una sola operación. |

---

### HU-1.2: Restricciones por rol, estado y reversión


| Atributo | Versión Original | Versión Refinada por GEMA A | Diferencias Detectadas |
| --- | --- | --- | --- |
| **Descripción** | Como administrador quiero que el sistema bloquee cambios de prioridad inválidos para asegurar el control de permisos y la integridad. | Como **Sistema de Seguridad**, quiero validar que solo **Administradores** modifiquen prioridades en estados permitidos, para prevenir accesos no autorizados y estados inconsistentes. | Se reenfocó como una historia de **reglas de dominio/seguridad**, especificando que el bloqueo ocurre a nivel de API (Ticket Service). |
| **Criterios de Aceptación** | El sistema bloquea la acción si el usuario no es Admin o el ticket está "Closed". No se puede volver a "Unassigned". | **Given** un ticket en estado `CLOSED`, **When** un Admin intenta cambiar prioridad, **Then** el sistema lanza la excepción `TicketAlreadyClosed` y devuelve `409 Conflict`. | Se integraron las excepciones técnicas ya definidas en el contexto de negocio (como `TicketAlreadyClosed`) para dar coherencia técnica. |

---

### HU-1.3: Visualizar justificación en el detalle del ticket

**Diagnóstico General:** Esta historia "No Cumple" con el criterio de Independencia, ya que depende de que la HU-1.1 capture el dato de justificación. Sin la modificación en la HU-1.1, esta historia no tiene datos que mostrar.

| Atributo | Versión Original | Versión Refinada por GEMA A | Diferencias Detectadas |
| --- | --- | --- | --- |
| **Descripción** | Como administrador quiero ver la justificación en el detalle del ticket cuando exista para entender el motivo del cambio. | Como **Agente o Administrador**, quiero visualizar el motivo de priorización en la vista de detalle, para tener contexto inmediato de la urgencia asignada. | Se amplió el beneficio al rol de **Agente**, ya que ellos son quienes resuelven el ticket y necesitan ese contexto. |
| **Criterios de Aceptación** | Scenario: Detalle muestra justificación cuando existe. When se renderiza el detalle, Then se muestra la justificación. | **Given** un ticket con `priority_reason` persistido, **When** el componente de Frontend carga los datos, **Then** se renderiza una etiqueta de "Motivo de Prioridad" con el texto correspondiente. | Se especificó el campo técnico (`priority_reason`) y el comportamiento esperado en el Frontend (React). |

---

## 3. Resumen del Análisis INVEST

| Criterio | Resultado | Comentario Técnico |
| --- | --- | --- |
| **Independent** | **Parcial** | Las historias 1.2 y 1.3 están acopladas funcionalmente a la 1.1. |
| **Negotiable** | **Cumple** | Permiten definir en el refinamiento qué tan larga debe ser la justificación. |
| **Valuable** | **Cumple** | Eliminan la deuda técnica de la "asignación aleatoria" mencionada en el contexto. |
| **Estimable** | **Cumple** | El stack (Django + React) permite implementar estos cambios de forma clara. |
| **Small** | **Cumple** | Cada una puede completarse en un Sprint, aunque se recomienda agruparlas. |
| **Testable** | **Cumple** | Los criterios Gherkin originales son una excelente base para pruebas automatizadas. |

---
