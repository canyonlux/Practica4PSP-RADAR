package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class CarSimulator implements Runnable {


    private final MqttClient clienteMqtt; //Cliente MQTT
    private final Random generadorAleatorio = new Random(); // Generador de números aleatorios

    public CarSimulator(String urlMqtt) throws MqttException { // Constructor del simulador
        this.clienteMqtt = new MqttClient(urlMqtt, UUID.randomUUID().toString()); // Cliente MQTT
        MqttConnectOptions opcionesConexion = new MqttConnectOptions(); // Opciones de conexión
        opcionesConexion.setAutomaticReconnect(true); // Reconexión automática
        opcionesConexion.setCleanSession(true); // Sesión limpia
        opcionesConexion.setConnectionTimeout(10);
        clienteMqtt.connect(opcionesConexion);
        clienteMqtt.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void messageArrived(String topic, MqttMessage mensajeMqtt) {
                // Procesar y mostrar mensajes de multas recibidos
                String[] multa = mensajeMqtt.toString().split(":");
                System.out.printf("(%s): %s - %.2f€%n",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                        multa[1], Double.parseDouble(multa[2]));
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        clienteMqtt.subscribe("vehiculo/multa");
    }



    @Override
    public void run() { // Método run del hilo
        do {
            try {
                // Generar velocidad y matrícula aleatorias para el vehículo
                int velocidad = generadorAleatorio.nextInt(60, 141);
                StringBuilder matricula = new StringBuilder();
                matricula.append(String.format("%04d", generadorAleatorio.nextInt(10000)));
                for (int i = 0; i < 3; i++) {
                    matricula.append((char)generadorAleatorio.nextInt('A', 'Z'+1));
                }

                // Publicar datos del vehículo si el cliente está conectado
                if(clienteMqtt.isConnected()) {
                    String topic = "datos/vehiculo";
                    byte[] payload = (matricula + ":" + velocidad).getBytes();
                    MqttMessage mensaje = new MqttMessage(payload);
                    mensaje.setQos(0);
                    mensaje.setRetained(true);
                    clienteMqtt.publish(topic, mensaje);
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        } while (true);
    }
}