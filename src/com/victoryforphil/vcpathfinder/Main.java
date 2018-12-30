package com.victoryforphil.vcpathfinder;

import java.util.ArrayList;
import java.util.List;

import com.victoryforphil.victoryconnect.Client;
import com.victoryforphil.victoryconnect.listeners.MDNSListener;
import com.victoryforphil.victoryconnect.listeners.PacketListener;
import com.victoryforphil.victoryconnect.networking.Packet;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.Trajectory.Segment;

public class Main {
    static Client vcClient = new Client("pathfinder", "Pathfinder");

    public static void main(String args[]) {
        // Params:
        /*
         * Timestep, Max Vel, Max Aceel, Max Jerk
         */

        vcClient.enableMDNS(new MDNSListener() {
            @Override
            public void onService(String type, String ip, String port) {
                if (type == "TCP") {
                    vcClient.enableTCP(ip, port);
                    vcClient.setTickRate(200);
                    vcClient.enableASAP();
                    regGen();
                }
            }
        });

       

     

    }
    static int trajIndex = 0;
    public static void regGen(){

        vcClient.newTopic("Pathfinder Status", "pathfinder/status", "TCP");
       
        vcClient.setTopic("pathfinder/status", "Ready");

        vcClient.registerCommand("pathfinder_generate", new PacketListener() {
            @Override
            public void onCommand(Packet packet) {
                String[] data = packet.data;
                System.out.println("GENING PATH");
                Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
                        Trajectory.Config.SAMPLES_HIGH, 3, 0.1, 2.0, 60.0);
                int index = 0;
                List<Waypoint> waypointsList = new ArrayList<>();
                for (String val : data) {
                    double x = 0;
                    double y = 0;
                    double rot = 0;
                    switch (index) {
                    case 0:
                        x = Double.parseDouble(val);
                    case 1:
                        y = Double.parseDouble(val);
                    case 2:
                        rot = Double.parseDouble(val);
                        rot = Pathfinder.d2r(rot);
                    }

                    waypointsList.add(new Waypoint(x, y, rot));
                  
                    index++;
                    if (index > 2) {
                        index = 0;
                    }
                }
                
                Waypoint[] waypoints = new Waypoint[waypointsList.size()];
                for(int i=0;i<waypointsList.size();i++){
                    waypoints[i] = waypointsList.get(i);
                }
                Trajectory trajectory = Pathfinder.generate(waypoints, config);
                ArrayList<String> toSend = new ArrayList<>();
              
                
                int count = 0;
                for (Segment seg : trajectory.segments) {
                    String path = "pathfinder/segments/"+count;
                    vcClient.newTopic("name", path, "TCP");
                    vcClient.setTopic(path, new Object[]{seg.x,seg.y,seg.dt,seg.heading,seg.acceleration,seg.jerk,seg.velocity});
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    count++;
                }
                
                System.out.println("Length: " + trajectory.segments.length);
                trajIndex ++;
                
            }
        });

    }
}