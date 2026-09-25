**Taller en clase – Arquitectura por capas**

**Grupo:** 2 personas ( Jose Rodriguez , Laura Perez)

**Puntos:** 40

**Entrega :** Documento Word, script de base de datos (Postgre)  y archivo .zip del proyecto.

**Gestión de parqueadero**

**Contexto**

Un parqueadero requiere un sistema de software para gestionar el ingreso, permanencia y pago de los vehículos.

Actualmente se manejan las siguientes tarifas:

- **Motocicleta:** $800
- **Automóvil:** $1.800

Las tarifas corresponden al valor cobrado por hora de parqueadero.

El valor de las tarifas puede cambiar cada año. Además, el propietario del parqueadero podría incorporar nuevos tipos de vehículos en el futuro, por ejemplo, camionetas u otros vehículos.

El sistema debe permitir registrar el ingreso de un vehículo, consultar la información del parqueadero y calcular el valor que debe pagar de acuerdo con el tipo de vehículo y el tiempo de permanencia.

El pago puede ser realizado de dos formas:

1. **Por el usuario:** el usuario consulta el valor y realiza el pago mediante el software.
2. **Por el personal del parqueadero:** un empleado registra el pago directamente en el sistema.

**Situación para resolver**

Diseñar una solución de software que permita gestionar las operaciones básicas del parqueadero y que pueda adaptarse a cambios en las tarifas y a la incorporación de nuevos tipos de vehículos.

La solución debe utilizar una **arquitectura de software por capas**.

**Actividades**

**1. Modelo de casos de uso nivel 0**

Elabore un **diagrama de casos de uso UML** que represente las principales funcionalidades del sistema. No es necesario implementar todos los casos de uso en el prototipo.

**2. Diseño de Datos- Modelo de clases**

Elabore un **diagrama de clases UML** que represente las principales entidades y responsabilidades identificadas para la solución.

**3. Diseño arquitectónico**

Diseñe una **arquitectura por capas** para la solución. Diagrama de paquetes

**5. Prototipo**

Implemente en **Java** un prototipo funcional que evidencie la arquitectura propuesta.

El prototipo puede ser:

- Aplicación de escritorio, o
- Aplicación Web. (elegimos esto)

No es necesario implementar un sistema completo de producción.

El prototipo debe demostrar como mínimo:

1. Registrar un vehículo.
2. Listar vehículos
3. Registrar su ingreso.
4. Registrar Salida

La implementación debe mantener la separación entre las capas definidas en la arquitectura.
