# Sistema de Simulación de Radar de Velocidad

Este sistema simula un radar de velocidad y la emisión de multas usando MQTT y Redis para la comunicación entre diferentes componentes. El sistema consta de tres aplicaciones principales:

1. **Simulador de Coches (`CarSimulator.java`)**: Simula vehículos que circulan a velocidades aleatorias.
2. **Radar Móvil (`Radar.java`)**: Detecta vehículos que superan el límite de velocidad y registra esta información.
3. **Central de Policía (`PoliceStation.java`)**: Procesa los excesos de velocidad y emite multas.

## Funcionalidades

### CarSimulator.java
- Envía cada segundo un mensaje MQTT con una velocidad aleatoria entre 60 y 140 km/h y una matrícula aleatoria.
- Muestra por pantalla las multas recibidas de `PoliceStation.java`.

### Radar.java
- Lee los mensajes MQTT con las velocidades.
- Registra en Redis los vehículos que superan los 80 km/h con una clave `EXCESO:velocidad:matricula`.
- Añade a un grupo `VEHICULOS` en Redis los vehículos que no superan la velocidad para estadísticas.

### PoliceStation.java
- Lee las claves de Redis `EXCESO:*`.
- Calcula y envía multas basadas en la velocidad excesiva:
    - 10%-20% sobre límite: 100 €
    - 20%-30% sobre límite: 200 €
    - 30% o más sobre límite: 500 €
- Elimina la clave de Redis tras enviar la multa y añade la matrícula a `VEHICULOSDENUNCIADOS`.
- Muestra estadísticas de vehículos multados y totales cada segundo.

## Requisitos

- Java JDK 8 o superior
- MQTT Broker (como Mosquitto)
- Redis

## Configuración

Configura tu broker MQTT y servidor Redis. Asegúrate de que están ejecutándose antes de iniciar las aplicaciones.


## Topics MQTT y Claves de Redis

- **MQTT Topics**:
    - `car/data`: Utilizado por `CarSimulator` para enviar datos de velocidad y matrícula.
    - `car/ticket`: Utilizado por `PoliceStation` para enviar multas.
- **Claves de Redis**:
    - `EXCESO:velocidad:matricula`: Utilizada por `Radar` para registrar excesos de velocidad.
    - `VEHICULOS`: Grupo para registrar vehículos que no exceden el límite de velocidad.
    - `VEHICULOSDENUNCIADOS`: Grupo para registrar matrículas de vehículos multados.

## Notas Adicionales

- Asegúrate de que todas las aplicaciones tengan acceso al broker MQTT y al servidor Redis.
- Puedes modificar los parámetros de configuración en el código según tus necesidades.

## Licencia



## Contacto

https://github.com/canyonlux
