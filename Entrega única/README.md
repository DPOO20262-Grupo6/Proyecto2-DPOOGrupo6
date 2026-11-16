# Club Deportivo Séneca · Proyecto 2 (Grupo 6)

Aplicación de consola en Java para el Club Deportivo Séneca. Tiene tres consolas, una por tipo de usuario, que comparten los mismos datos.

## Cómo ejecutar

El proyecto Eclipse es la carpeta del repositorio; el código está en `Entrega única/src`. Requiere Java y Eclipse. Las pruebas usan JUnit 5 (el proyecto ya lo declara en el `.classpath`).

### Las tres consolas

| Consola | Clase (en `src/presentacion`) | Usuario |
|---|---|---|
| Administrador | `ConsolaAdministrador` | Administrador |
| Empleado | `ConsolaEmpleado` | Entrenadores y fisioterapeutas |
| Socio | `ConsolaSocio` | Socios |

Cada una tiene su propio `main`. Para ejecutar una: clic derecho sobre la clase → **Run As → Java Application**. No hace falta configurar nada más. La consola se maneja escribiendo en la pestaña **Console** de Eclipse, y se sale con la opción `0`.

### Los datos

Los datos están en `Entrega única/datos`. Las tres consolas los encuentran solas, se ejecuten desde donde se ejecuten: al iniciar los cargan y al salir (opción `0`) guardan los cambios en el mismo archivo. No se pide ningún nombre de archivo. Antes de probar, haz una copia de esa carpeta; al terminar, restáurala para no ensuciar lo que se entrega.

### Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador |
| `ent1`, `ent2`, `ent3` | `123` | Entrenadores |
| `fisio1` | `123` | Fisioterapeuta |
| `socioA1` … `socioA8`, `socioB1` … `socioB8` | `clave1` … `clave8` (el número del usuario) | Socios |

Tres intentos fallidos de inicio de sesión cierran la consola. Escribir `cancelar` en cualquier pregunta abandona la operación sin cambiar nada.

### Pruebas automáticas

Clic derecho sobre `Entrega única/src/pruebas` → **Run As → JUnit Test**. Todas las pruebas deben salir en verde.

## Lista de pruebas manuales

Hacer en este orden. Marca cada una cuando salga como se describe.

### 1. Administrador

- [ ] Entrar con `admin` / `admin123`.
- [ ] Crear un torneo de TENIS, `INDIVIDUAL`, recreativo, con bono `15000` y fecha futura. Debe mostrar el id `T-1`.
- [ ] Crear un torneo de FUTBOL, categoría `MAYORES`, competitivo, tarifa `20000`. Debe mostrar `T-2`.
- [ ] Consultar el estado y el historial de `T-1`.
- [ ] Salir con `0`: debe decir que guardó los datos.

### 2. Empleado

- [ ] Entrar con `ent1` / `123`. Contraseña errada: debe rechazar sin decir qué dato falló.
- [ ] Con el usuario de un socio en esta consola: debe rechazarlo igual.
- [ ] Torneos → ver los disponibles: aparece `T-1` con costo `$0` y no aparece `T-2`.
- [ ] Inscribirse en `T-1`: aviso de que no opta al premio en efectivo y «Valor pagado: $0.0».
- [ ] Inscribirse otra vez en `T-1`: «Ya esta inscrito».
- [ ] Inscribirse en `T-2`: debe rechazar, los empleados solo participan en torneos individuales.
- [ ] Ver mis torneos e historial. Retirarse de `T-1`: el cupo queda libre.
- [ ] Cerrar con `0` y volver a entrar: el estado se conserva.

### 3. Socio

- [ ] Entrar con `socioA1` / `clave1`.
- [ ] Torneos → inscribirme como jugador en `T-1`.
- [ ] Torneos → inscribir a mi equipo en `T-2`.
- [ ] Torneos → inscribir a un invitado `EXTERNO` con el documento `9001`. Repetir el documento debe rechazarlo.
- [ ] Ver mis torneos e historial de `T-1`. Retirarme de `T-1`.
- [ ] Compras → tienda: comprar un balón (total $95.200), y otra compra redimiendo puntos.
- [ ] Compras → tienda: la cuerda no tiene existencias, debe rechazar la compra.
- [ ] Academias y reservas: inscribirme en una academia donde ya estoy debe rechazar por duplicada.
- [ ] Partidos y ranking: consultar el ranking de TENIS.
- [ ] Cerrar con `0` y volver a entrar: las inscripciones se conservan.

### 4. Entre consolas

- [ ] Lo que crea el administrador (torneos) lo ven el empleado y el socio.
- [ ] La inscripción de un empleado aparece en el historial que ve el administrador.
- [ ] Cada consola guarda al salir y la siguiente carga lo guardado.

### Lo que no se puede ver por consola

Ninguna consola registra partidos ni finaliza torneos. Ganar y usar un bono, el premio en efectivo y las reglas de turno de los empleados se comprueban con las pruebas automáticas (`TestTorneosSocio`, `ComprasSocioTest`, `TestTorneosEmpleado`).

## Estructura del código (`src`)

| Paquete | Qué contiene |
|---|---|
| `presentacion` | Las tres consolas y las clases base de menú y validación de entradas |
| `autenticacion` | Inicio de sesión compartido por las tres consolas |
| `persistencia` | Carga y guardado de los datos |
| `usuarios`, `club` | Usuarios y el club, que reúne todos los datos |
| `Torneos`, `inscripciones` | Torneos internos, inscripciones y reglas de empleados y socios |
| `articulosYVentas` | Tienda, cafetería, ventas, puntos y bonos |
| `deportesDeConjunto`, `deportesIndividuales`, `entrenamiento`, `instalaciones`, `estadisticas`, `inventario`, `administracion` | Resto del dominio |
| `pruebas` | Pruebas JUnit |

Los diagramas, los documentos de diseño y las historias de usuario están en `docs_y_diagramas`.
