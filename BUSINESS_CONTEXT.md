# Plantilla de Contexto de Negocio — Sistema de Tickets

## 1. Descripción del Proyecto

### Nombre del Proyecto
**SistemaTickets** – Arquitectura de Microservicios para Gestión de Tickets

### Objetivo del Proyecto
Facilitar la gestión integral de tickets de soporte y solicitudes en una arquitectura escalable, desacoplada y de bajo acoplamiento, mediante microservicios independientes que se comunican de forma asíncrona. El objetivo es permitir que usuarios registren tickets, que estos se asignen automáticamente a agentes, y que los usuarios reciban notificaciones sobre el estado de sus solicitudes, todo esto de manera eficiente, resiliente y fácil de mantener.

---

## 2. Flujos Críticos del Negocio

### Principales Flujos de Trabajo

#### 2.1 Flujo de Creación de Tickets
1. **Usuario inicia sesión** en la aplicación web
2. **Completa formulario de nuevo ticket** con:
   - Título del problema
   - Descripción detallada
3. **Frontend envía petición** al Ticket Service (API REST)
4. **Ticket Service crea el ticket** con estado inicial `OPEN`
5. **Ticket Service publica evento** `ticket.created` en RabbitMQ
6. **Flujo asíncrono paralelo:**
   - Assignment Service consume evento y **asigna automáticamente un agente**
   - Notification Service consume evento y **crea una notificación** para el usuario
7. **Frontend recibe respuesta** 201 Created con datos del ticket
8. **Usuario puede consultar** estado del ticket desde el dashboard

#### 2.2 Flujo de Cambio de Estado de Ticket
1. **Agente actualiza estado del ticket** (OPEN → IN_PROGRESS → CLOSED)
2. **Frontend envía PATCH request** al Ticket Service
3. **Ticket Service valida el cambio:**
   - Rechaza cambios en tickets CLOSED
   - Mantiene idempotencia (si ya tiene ese estado, no hace nada)
4. **Si cambio es válido:**
   - Persiste nuevo estado en base de datos
   - Publica evento `ticket.status_changed` en RabbitMQ
   - Notification Service genera notificación de cambio de estado hacia el usuario
5. **Frontend refleja el cambio** en tiempo real (mediante polling o WebSocket)

#### 2.3 Flujo de Asignación de Tickets
1. **Assignment Service recibe evento** `ticket.created` de RabbitMQ
2. **Ejecuta lógica de asignación** (actualmente aleatoria, preparada para evolucionar)
3. **Asigna ticket a un agente** basándose en:
   - Disponibilidad del agente
   - Carga actual de trabajo
   - SLA y roles (estructura preparada, no totalmente implementada)
4. **Persiste asignación** en su base de datos independiente
5. **Agents pueden consultar** todos sus tickets asignados

#### 2.4 Flujo de Notificaciones
1. **Notification Service consume eventos** de RabbitMQ (`ticket.created`, `ticket.status_changed`)
2. **Crea registros de notificación** en su base de datos
3. **Marca notificaciones como leídas** cuando el usuario las visualiza (operación idempotente)
4. **Infraestructura preparada** para enviar:
   - Notificaciones en la aplicación (implementado)
   - Emails/SMS (preparado pero no implementado)
5. **Frontend consulta notificaciones** mediante API REST en tiempo real

### Módulos o Funcionalidades Críticas

| Módulo | Responsabilidad |
|--------|-----------------|
| **Ticket Service** | Creación, consulta, cambio de estado de tickets. Publica eventos críticos. Implementación DDD de referencia. |
| **Assignment Service** | Asignación automática de tickets a agentes. Consulta de asignaciones. Preparado para reglas sofisticadas de priorización. |
| **Notification Service** | Generación y persistencia de notificaciones. Infraestructura para múltiples canales (app, email, SMS). |
| **User Service** | Gestión completa de usuarios (CRUD). Roles y permisos. Autenticación y autorización. |
| **Frontend (React+Vite)** | Interfaz de usuario para crear tickets, consultar estados, recibir notificaciones. Áreas para agentes y usuarios regulares. |

---

## 3. Reglas de Negocio y Restricciones

### Reglas de Negocio Relevantes

#### Tickets
- **Estados válidos:** Un ticket transita a través de tres estados:
  - `OPEN`: Ticket recién creado, aún sin asignar o en espera de acción
  - `IN_PROGRESS`: Un agente está trabajando en resolver el ticket
  - `CLOSED`: Ticket resuelto, ya no puede cambiar de estado
  
- **Restricción crítica:** Un ticket en estado `CLOSED` **NO puede cambiar de estado bajo ninguna circunstancia**. Si se intenta cambiar lanza una excepción de dominio `TicketAlreadyClosed`.

- **Creación:** Todos los tickets nuevos inician en estado `OPEN` automáticamente.

- **Campos obligatorios:** Un ticket requiere obligatoriamente:
  - Título (no vacío, descripción del problema)
  - Descripción (detalles del problema)
  - Usuario creador (identificado por su sesión)

- **Idempotencia:** Si un ticket ya posee el estado que se intenta cambiarle, la operación **no genera cambio ni publica evento**. Devuelve la representación actual sin modificar.

- **Eventos:** Tras cada operación significativa, se publican eventos:
  - `ticket.created`: Al crear un nuevo ticket
  - `ticket.status_changed`: Al cambiar estado (solo si cambió realmente)

#### Asignación de Tickets
- **Automaticidad:** Cuando se registra un ticket nuevo (evento `ticket.created`), **debe asignarse automáticamente a un agente** dentro de 5 segundos.

- **Algoritmo actual:** Asignación aleatoria entre agentes disponibles (placeholder).

- **Futuro:** El algoritmo evolucionará para considerar:
  - Carga de trabajo del agente (menos tickets asignados = más prioridad)
  - SLA del cliente (tickets urgentes se asignan primero)
  - Competencias del agente (roles especializados)
  - Disponibilidad y horarios

- **Idempotencia:** Si un ticket ya está asignado, no se reasigna al recibir eventos duplicados.

#### Notificaciones
- **Generación:** Se genera una notificación automática cuando:
  - Se crea un ticket (informa al usuario que fue registrado)
  - Cambia el estado del ticket (informa progreso a usuario)

- **Persistencia:** Todas las notificaciones se guardan en base de datos para consulta histórica.

- **Lectura:** Al usuario marcar una notificación como leída, la operación es **idempotente** (no falla si ya estaba leída).

- **Canales:** Infraestructura preparada para enviar notificaciones a través de:
  - Aplicación (implementado)
  - Email (preparado, no implementado)
  - SMS/Mensajería (preparado, no implementado)

#### Usuarios
- **Roles:** Existen dos roles principales en el sistema:
  - **Usuario estándar:** Puede crear tickets, consultar sus propios tickets, recibir notificaciones
  - **Agente:** Puede consultar todos los tickets asignados, cambiar sus estados, ver asignaciones

- **Autenticación:** Todos los usuarios deben autenticarse vía login antes de acceder al sistema.

- **Segregación de datos:** Cada usuario estándar solo ve sus propios tickets; los agentes ven los tickets asignados a ellos.

### Regulaciones o Normativas

- **Protección de datos:** El sistema debe cumplir con principios de privacidad:
  - No exponer información de usuarios a través de APIs públicas
  - Solo User Service puede estar autorizado para consultar datos de usuarios
  - Otros servicios acceden mediante llamadas API con autenticación

- **Access Control:** 
  - Solo agentes autenticados pueden cambiar estados de tickets
  - Solo el creador del ticket (o un agente asignado) puede ver sus detalles
  - Logs de auditoría de cambios de estado y asignaciones (preparado en AUDITORIA.md)

- **Integridad de datos:**
  - Transacciones atómicas: Un ticket se crea y publica evento, o ninguna de las dos cosas
  - Consistencia eventual: La asignación puede tomar algunos segundos

---

## 4. Perfiles de Usuario y Roles

### Perfiles o Roles de Usuario en el Sistema

#### 4.1 Usuario Estándar (Standard User)

**Descripción:** Cliente o usuario final que crea tickets de soporte.

**Capacidades:**
- ✅ Crear nuevos tickets
- ✅ Consultar sus propios tickets (lista y detalles)
- ✅ Ver notificaciones sobre sus tickets
- ✅ Marcar notificaciones como leídas
- ✅ Actualizar su perfil (email, contraseña)
- ✅ Cerrar sesión

**Restricciones:**
- ❌ No puede crear usuarios o gestionar usuarios
- ❌ No puede cambiar manualmente el estado de sus tickets
- ❌ No puede asignar tickets a agentes
- ❌ No puede ver tickets de otros usuarios
- ❌ No puede acceder a reportes de sistema o métricas

#### 4.2 Agente (Agent User)

**Descripción:** Personal del equipo de soporte encargado de resolver tickets.

**Capacidades:**
- ✅ Consultar todos sus tickets asignados
- ✅ Cambiar estado de tickets (OPEN → IN_PROGRESS → CLOSED)
- ✅ Ver historial de cambios de estado
- ✅ Recibir notificaciones de nuevos tickets asignados
- ✅ Consultar detalles de la asignación (prioridad, SLA)
- ✅ Actualizar su perfil

**Restricciones:**
- ❌ No puede crear nuevos tickets (aunque podría en futuro)
- ❌ No puede reasignarse manualmente un ticket
- ❌ No puede ver tickets de otros agentes
- ❌ No puede cambiar estado de un ticket que no le está asignado
- ❌ No puede crear nuevos agentes

#### 4.3 Administrador (Admin)

**Descripción:** Personal administrativo con acceso completo al sistema.

**Capacidades (futuras):**
- ✅ Crear, editar y eliminar usuarios
- ✅ Asignar o reasignar tickets manualmente
- ✅ Cambiar roles de usuarios
- ✅ Ver reportes y métricas
- ✅ Configurar reglas de negocio (SLA, prioridades)
- ✅ Acceder a logs de auditoría

**Nota:** Este rol está preparado en la arquitectura pero aún no implementado en la interfaz.

### Permisos y Limitaciones de Cada Perfil

| Acción | Usuario Estándar | Agente | Admin |
|--------|------------------|--------|-------|
| Crear ticket | ✅ | ❌ | ✅ |
| Consultar propio ticket | ✅ | N/A | ✅ |
| Consultar ticket asignado | N/A | ✅ | ✅ |
| Cambiar estado ticket | ❌ | ✅ | ✅ |
| Crear usuario | ❌ | ❌ | ✅ |
| Gestionar roles | ❌ | ❌ | ✅ |
| Ver notificaciones propias | ✅ | ✅ | ✅ |
| Ver reportes | ❌ | ❌ | ✅ |
| Acceder a auditoria | ❌ | ❌ | ✅ |
| Configurar reglas de negocio | ❌ | ❌ | ✅ |

---

## 5. Condiciones del Entorno Técnico

### Plataformas Soportadas

- **Plataforma primaria:** Web (Navegador moderno)
  - Navegadores soportados: Chrome, Firefox, Safari, Edge (últimas 2 versiones)
  - Responsivo para desktop y tablet
  
- **Dispositivos:**
  - Desktop (Windows, macOS, Linux)
  - Tablet (iPad, Android tablets)
  - Móvil: Diseño responsive, aunque aplicación móvil nativa no está en roadmap inmediato

- **Requisitos del navegador:**
  - JavaScript habilitado
  - Cookies habilitadas (para sesiones)
  - Soporte para Fetch API y ES2020+

### Tecnologías o Integraciones Clave

#### Backend - Stack Microservicios

| Componente | Tecnología | Versión | Propósito |
|-----------|-----------|---------|----------|
| **Framework Web** | Django REST Framework | 5.x | API REST, validación, serialización |
| **Lenguaje** | Python | 3.11+ | Implementación de servicios |
| **Base de Datos** | PostgreSQL | 16 | Persistencia (4 instancias independientes) |
| **Message Broker** | RabbitMQ | 3.x | Comunicación asíncrona entre servicios |
| **Librería Messaging** | Pika | 1.x | Driver Python para RabbitMQ |
| **Contenerización** | Docker | 24+ | Aislamiento y deployment |
| **Orquestación** | Docker Compose | 2.x | Local development |
| **Testing Backend** | Pytest | 7.x+ | Validación de lógica de negocio |

#### Frontend - Stack SPA

| Componente | Tecnología | Versión | Propósito |
|-----------|-----------|---------|----------|
| **Framework** | React | 19.x | UI componentizada |
| **Lenguaje** | TypeScript | 5.x | Type safety |
| **Build Tool** | Vite | 5.x+ | Bundling y desarrollo rápido |
| **Router** | React Router | 7.x | Navegación SPA |
| **Testing Frontend** | Vitest + React Testing Library | 1.x+ | Unit y integration tests |
| **Estilos** | CSS Modules + CSS Puro | - | Estilos encapsulados |
| **HTTP Client** | Fetch API (nativa) | - | Comunicación con APIs |

#### Integraciones Externas (Preparadas pero no implementadas)

- **Servicios de Email:** Estructura lista para integrar SMTP o servicios como SendGrid
- **Servicios de SMS:** Estructura preparada para Twilio o similares
- **Autenticación OAuth:** Preparado para extender con OAuth2/SAML
- **Analytics:** Dashboard de métricas (infraestructura en AUDITORIA.md)

#### Patrones Arquitectónicos Implementados

- **Domain-Driven Design (DDD):** Ticket Service implementa completamente; otros servicios en evolución
- **Event-Driven Architecture (EDA):** Comunicación asíncrona mediante eventos de dominio
- **Database per Service:** Cada microservicio tiene su propia BD PostgreSQL
- **API Gateway Pattern:** Frontend consume múltiples servicios
- **Repository Pattern:** Abstracción de persistencia
- **Factory Pattern:** Creación validada de entidades

#### Configuración de RabbitMQ

- **Exchange tipo:** Fanout (replica mensaje a todas las colas suscritas)
- **Patrón de Consumo:** Consumidores dedicados por servicio
- **Durabilidad:** Colas durables para tolerar fallos de consumidor
- **Reintento:** Lógica de reconexión automática (en evolución)
- **Dead Letter Queue:** Preparado para manejar mensajes problemáticos

---

## 6. Casos Especiales o Excepciones

### Excepciones y Escenarios Alternos Críticos

#### 6.1 Ticket ya Cerrado (Excepción Crítica)
**Escenario:** Un agente intenta cambiar el estado de un ticket que ya está `CLOSED`

**Comportamiento:**
- Sistema rechaza la operación inmediatamente
- Lanza excepción de dominio: `TicketAlreadyClosed`
- No se persiste ningún cambio
- No se publica evento
- Frontend recibe error HTTP 409 (Conflict) con mensaje claro
- Notificación automática al agente indicando que el ticket ya está cerrado

#### 6.2 Idempotencia en Cambio de Estado
**Escenario:** Agente intenta cambiar un ticket de `IN_PROGRESS` a `IN_PROGRESS` (mismo estado)

**Comportamiento:**
- Sistema detecta que el estado no cambió
- No persiste registro de cambio
- No publica evento `ticket.status_changed`
- Devuelve HTTP 200 OK con representación actual (sin marcar como modificado)
- Frontend puede detectar esto y no mostrar "cambio reciente"

#### 6.3 Fallo de RabbitMQ Después de Crear Ticket
**Escenario:** Ticket se crea en BD pero falla la publicación de evento

**Comportamiento:**
- Ticket persiste correctamente en BD
- Excepción en publicación es logueda (LOG ERROR)
- Frontend recibe HTTP 201 Created (el ticket se creó)
- **Inconsistencia eventual:** Asignación y notificación no ocurren inmediatamente
- Sistema debe incluir:
  - Mecanismo de reintentos automáticos
  - Job de reconciliación para detectar tickets sin asignar
  - Alerta en monitoreo (preparado en CALIDAD.md)

#### 6.4 Consumidor de Events Desconectado
**Escenario:** El Assignment Service se desconecta de RabbitMQ durante 2 minutos

**Comportamiento:**
- Mensajes se acumulan en la cola de RabbitMQ (durables)
- Cuando Assignment Service se reconecta, procesa todos los mensajes acumulados
- **Riesgo:** Retrasos en asignación, pero no pérdida de datos
- **Mitigación:** Lógica de reconexión automática + dead letter queue para fallos persistentes

#### 6.5 Usuario Estándar Intenta Cambiar Estado de su Ticket
**Escenario:** Un usuario intenta enviar PATCH para cambiar estado de su propio ticket

**Comportamiento:**
- Backend valida autorización antes de procesar
- Si usuario no es agente asignado, rechaza con HTTP 403 Forbidden
- No se realiza cambio alguno
- Error es loguado para auditoría

#### 6.6 Creación de Ticket sin Descripción
**Escenario:** Frontend envía POST a `/api/tickets/` sin campo `description`

**Comportamiento:**
- Django REST Framework valida campos obligatorios
- Devuelve HTTP 400 Bad Request
- Incluye detalle de campos faltantes: `{"description": ["Este campo es obligatorio."]}`
- Ticket NO se crea
- Evento NO se publica

#### 6.7 Usuario Intenta Ver Ticket de Otro Usuario
**Escenario:** Usuario 1 intenta GET `/api/tickets/{ticket_id_de_usuario_2}/`

**Comportamiento:**
- Backend valida que el usuario sea propietario del ticket O sea un agente asignado
- Si no cumple condición, devuelve HTTP 403 Forbidden o HTTP 404 Not Found
- No expone información del ticket a usuario no autorizado
- Acceso se registra en logs (preparado para auditoría)

#### 6.8 Asignación cuando no hay Agentes Disponibles
**Escenario:** Se crea un ticket pero todos los agentes tienen estado `offline` o están en su límite de carga

**Comportamiento actual:**
- Asignación aún ocurre (aleatoria)
- Ticket se asigna a un agente (aunque esté offline)
- **Problema** registrado en DEUDA_TECNICA.md

**Comportamiento esperado (futuro):**
- Ticket entra en cola de espera (estado `PENDING_ASSIGNMENT`)
- Se reasigna automáticamente cuando un agente vuelva a estar disponible
- Notificación al agente cuando se le asigne

#### 6.9 Notificación Duplicada por Evento Duplicado
**Escenario:** RabbitMQ entrega el mismo evento `ticket.created` dos veces

**Comportamiento:**
- Notification Service intenta crear dos notificaciones con el mismo ticket_id
- Lógica de idempotencia detecta duplicado (clave única en BD)
- Solo se crea una notificación
- No se muestra error al usuario
- Evento extra se descarta silenciosamente

#### 6.10 Cambio de Rol en Mitad de Sesión
**Escenario:** Un usuario que era estándar es promovido a agente mientras está usando la app

**Comportamiento actual:**
- Cambio de rol ocurre en User Service
- Usuario actual NO lo ve reflejado hasta recargar la página
- **Problema** registrado en DEUDA_TECNICA.md

**Comportamiento esperado (futuro):**
- User Service publica evento `user.roleChanged`
- Frontend recibe notificación y fuerza refresh de permisos
- UI se actualiza automáticamente sin reload

---

## Matriz de Trazabilidad: Reglas de Negocio vs Servicios

| Regla de Negocio | Ticket Service | Assignment Service | Notification Service | User Service |
|------------------|---|---|---|---|
| Estados OPEN → IN_PROGRESS → CLOSED | ✅ Valida | - | - | - |
| Rechaza cambios en CLOSED | ✅ Valida | - | - | - |
| Ticket nuevo en OPEN | ✅ Implementa | - | - | - |
| Publicar ticket.created | ✅ Publica | - | - | - |
| Asignar agente en ticket.created | - | ✅ Consume | - | - |
| Generar notificación en eventos | - | - | ✅ Consume | - |
| Segregación de datos por rol | ❓ Aplicable | ❓ Aplicable | ❓ Aplicable | ✅ Valida |
| Idempotencia en cambios de estado | ✅ Implementa | - | - | - |

---

## Conclusiones

El **SistemaTickets** es una arquitectura de referencia para sistemas de soporte escalables utilizando microservicios desacoplados. Sus puntos fuertes están en:

1. **Separación de responsabilidades:** Cada servicio tiene un dominio bien definido
2. **Comunicación asíncrona:** Reduce acoplamiento e incrementa resilencia
3. **Domain-Driven Design:** Ticket Service es modelo a replicar en otros servicios
4. **Testabilidad y documentación:** Código limpio con arquitectura explícita

Las áreas de evolución están documentadas en:
- [DEUDA_TECNICA.md](./DEUDA_TECNICA.md) – Trabajo futuro
- [AUDITORIA.md](./AUDITORIA.md) – Riesgos identificados
- [CALIDAD.md](./CALIDAD.md) – Métricas y validación

