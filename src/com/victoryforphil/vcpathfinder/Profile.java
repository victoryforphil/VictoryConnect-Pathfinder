package com.victoryforphil.vcpathfinder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.victoryforphil.victoryconnect.Client;

//Timestep, Max Vel, Max Aceel, Max Jerk
public class Profile{
    private double timeStep = 0.05;
    private double maxVel   = 2.0;
    private double maxAccel = 2.0;
    private double maxJerk  = 60.0;


    private String name;
    public Profile(String name) {
        this.name = name;

       
    }


    /**
     * @return the timeStep
     */
    public double getTimeStep() {
        return timeStep;
    }

    /**
     * @param timeStep the timeStep to set
     */
    public void setTimeStep(double timeStep) {
        this.timeStep = timeStep;
       
    }

    /**
     * @return the maxVel
     */
    public double getMaxVel() {
        return maxVel;
    }

    /**
     * @param maxVel the maxVel to set
     */
    public void setMaxVel(double maxVel) {
        this.maxVel = maxVel;
       
    }

    /**
     * @return the maxAccel
     */
    public double getMaxAccel() {
        return maxAccel;
    }

    /**
     * @param maxAccel the maxAccel to set
     */
    public void setMaxAccel(double maxAccel) {
        this.maxAccel = maxAccel;
      
    }

    /**
     * @return the maxJerk
     */
    public double getMaxJerk() {
        return maxJerk;
    }

    /**
     * @param maxJerk the maxJerk to set
     */
    public void setMaxJerk(double maxJerk) {
        this.maxJerk = maxJerk;
       
    }

    
}