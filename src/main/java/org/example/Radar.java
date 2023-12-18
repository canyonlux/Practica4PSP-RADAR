package org.example;

import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;
import java.util.UUID;


public class Radar implements Runnable { // Radar


    private MqttClient clienteMqtt; // Cliente MQTT
    private int velocidadVehiculo; // Velocidad detectada del vehículo
    private String matriculaVehiculo; // Matrícula del vehículo
    private final String urlRedis; // URL para la conexión con Redis
    public static final String VEHICLES = "RUBEN:VEHICLES";










    // Constructor del ControladorRadar
    public Radar(String urlMqtt, String urlRedis) throws MqttException {
        this.urlRedis = urlRedis;
        this.clienteMqtt = new MqttClient(urlMqtt, UUID.randomUUID().toString());
        MqttConnectOptions opciones = new MqttConnectOptions();
        opciones.setAutomaticReconnect(true);
        opciones.setCleanSession(true);
        opciones.setConnectionTimeout(10);
        clienteMqtt.connect(opciones);
        clienteMqtt.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void messageArrived(String topic, MqttMessage mensajeMqtt) {
                String[] datos = mensajeMqtt.toString().split(":");
                matriculaVehiculo = datos[0];
                velocidadVehiculo = Integer.parseInt(datos[1]);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        clienteMqtt.subscribe("datos/vehiculo");
    }

    @Override
    public void run() { // Método run del hilo
        try (Jedis jedis = new Jedis(urlRedis, 6379)) {
            do {
                try {
                    // Procesar datos si se ha detectado un vehículo
                    if (matriculaVehiculo != null) {
                        // Publicar mensaje si el vehículo excede el límite de velocidad
                        if (velocidadVehiculo > 80) {
                            String mensaje = String.format("EXCESO:%d:%s", velocidadVehiculo, matriculaVehiculo);
                            MqttMessage mensajeMqtt = new MqttMessage(mensaje.getBytes());
                            clienteMqtt.publish("vehiculo/exceso", mensajeMqtt);
                        }
                        // Registrar la matrícula del vehículo en Redis
                        jedis.rpush(VEHICLES, matriculaVehiculo);
                        matriculaVehiculo = null;
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            } while (true);
        } catch (Exception e) {
            System.err.println("Error en la conexión con Redis: " + e.getMessage());
        }
    }
}