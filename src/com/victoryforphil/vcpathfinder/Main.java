package com.victoryforphil.vcpathfinder;

import java.util.ArrayList;
import java.util.HashMap;
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
    static HashMap<String, Profile> profiles = new HashMap<>();
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
                   
                    regGen();
                    initProfile();
                }
            }
        });

        vcClient.enableASAP();

     

    }


    public static void initProfile(){
        vcClient.registerCommand("pathfinder_profile", new PacketListener(){  
            @Override
            public void onCommand(Packet packet) {
            
                String name = packet.data[0];
                System.out.println("New Profile: " + name);
                double time = Double.parseDouble(packet.data[1]);
                double vel  = Double.parseDouble(packet.data[2]);
                double accel = Double.parseDouble(packet.data[3]);
                double jerk = Double.parseDouble(packet.data[4]);
                Profile profile = new Profile(name);
                profile.setMaxAccel(accel);
                profile.setMaxJerk(jerk);
                profile.setMaxVel(vel);
                profile.setTimeStep(time);
               
                profiles.put(name, profile);
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
               
                int index = 0;
                List<Waypoint> waypointsList = new ArrayList<>();
                Profile profile = null;
                for (String val : data) {
                    double x = 0;
                    double y = 0;
                    double rot = 0;
                    switch (index) {
                    case 0:
                        profile = profiles.get(val);
                    break;

                    case 1:
                        x = Double.parseDouble(val);
                    case 2:
                        y = Double.parseDouble(val);
                    case 3:
                        rot = Double.parseDouble(val);
                        rot = Pathfinder.d2r(rot);
                    }

                    waypointsList.add(new Waypoint(x, y, rot));
                  
                    index++;
                    if (index > 3) {
                        index = 1;
                    }
                }

                if(profile == null){
                    System.out.println("No profile loaded!");
                    return;
                }
                Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
                Trajectory.Config.SAMPLES_HIGH, profile.getTimeStep(), profile.getMaxVel(), profile.getMaxAccel(), profile.getMaxJerk());
                Waypoint[] waypoints = new Waypoint[waypointsList.size()];
                for(int i=0;i<waypointsList.size();i++){
                    waypoints[i] = waypointsList.get(i);
                }
                Trajectory trajectory = Pathfinder.generate(waypoints, config);
                ArrayList<String> toSend = new ArrayList<>();

                vcClient.newTopic("Pathfinder Trajector" + trajIndex, "pathfinder/trajectory", "TCP");
                for (Segment seg : trajectory.segments) {
                    toSend.add(Double.toString(seg.x));
                    toSend.add(Double.toString(seg.y));
                    toSend.add(Double.toString(seg.acceleration));
                    toSend.add(Double.toString(seg.dt));
                    toSend.add(Double.toString(seg.heading));
                    toSend.add(Double.toString(seg.position));
                    toSend.add(Double.toString(seg.velocity));
                }
                
                vcClient.setTopic("pathfinder/trajectory", toSend.toArray());
                trajIndex ++;
                System.out.println("Data: " + trajectory.length() * 7);
            }
        });

    }
}