package org.example;

import org.eclipse.paho.client.mqttv3.MqttException;


public class Main {


    public static void main(String[] args) {
//String url = "localhost";
        String url = "34.228.162.124";
        String mqttUrl = String.format("tcp://%s:1883", url); // MQTT broker
        System.out.println("URL: Conexion realizada" + mqttUrl);




        CarSimulator simulator;  // Car simulator
        try {
            simulator = new CarSimulator(mqttUrl);
        } catch (MqttException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        Radar radar; // Radar
        try {
            radar = new Radar(mqttUrl, url);
        } catch (MqttException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        PoliceStation policeStation; // Police station
        try {
            policeStation = new PoliceStation(mqttUrl, url);
        } catch (MqttException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }


        Thread simulatorThread = new Thread(simulator); // Threads
        Thread radarThread = new Thread(radar);
        Thread policeStationThread = new Thread(policeStation);


        simulatorThread.start();
        radarThread.start();
        policeStationThread.start();
    }
}