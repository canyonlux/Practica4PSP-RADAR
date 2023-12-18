package org.example;

import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;



public class PoliceStation implements Runnable {




    private final MqttClient clienteMqtt; // Cliente MQTT
    private final Jedis jedis; // Cliente de Redis


    public static final String VEHICULOS = "RUBEN:VEHICLES";


    public static final String VEHICULOS_MULTADOS = "RUBEN:FINEDVEHICLES";



    // Constructor de la comisaría
    public PoliceStation(String urlMqtt, String urlRedis) throws MqttException {
        this.clienteMqtt = new MqttClient(urlMqtt, UUID.randomUUID().toString());
        MqttConnectOptions opcionesConexion = new MqttConnectOptions();
        opcionesConexion.setAutomaticReconnect(true);
        opcionesConexion.setCleanSession(true);
        opcionesConexion.setConnectionTimeout(10);
        clienteMqtt.connect(opcionesConexion);
        clienteMqtt.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {}
            @Override
            public void messageArrived(String topic, MqttMessage mensajeMqtt) throws Exception {
                // Procesar y calcular multas basadas en el exceso de velocidad
                String[] datos = mensajeMqtt.toString().split(":");
                if(datos[0].equals("EXCESS") && datos[2] != null) {
                    int porcentajeExceso = (10000 / Integer.parseInt(datos[1])) - 100;
                    int multa;
                    if (porcentajeExceso >= 10 && porcentajeExceso < 20) {
                        multa = 100;
                    } else if (porcentajeExceso >= 20 && porcentajeExceso < 30) {
                        multa = 200;
                    } else {
                        multa = 300;
                    }
                    String mensaje = String.format("MULTA:%s:%d", datos[2], multa);
                    MqttMessage mensajeMulta = new MqttMessage(mensaje.getBytes());
                    clienteMqtt.publish("vehiculo/multa", mensajeMulta);
                    jedis.rpush(VEHICULOS_MULTADOS, datos[2]);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        clienteMqtt.subscribe("vehiculo/exceso");
        this.jedis = new Jedis(urlRedis, 6379);
        jedis.del(VEHICULOS_MULTADOS, VEHICULOS);
    }

    @Override
    public void run() { // Método run del hilo
        do {
            try {
                // Cálculo y presentación de estadísticas
                long totalVehiculos = jedis.llen(VEHICULOS);
                long totalVehiculosMultados = jedis.llen(VEHICULOS_MULTADOS);
                double porcentajeMultados = (double)totalVehiculosMultados / totalVehiculos * 100;
                System.out.printf("Total de vehículos: %d\n", totalVehiculos);
                System.out.printf("Total de multas: %.2f%% (%d vehículos multados)\n", totalVehiculosMultados == 0 ? 0 : porcentajeMultados, totalVehiculosMultados);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        } while (true);
    }
}