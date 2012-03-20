package edu.umich.sbolt.world;

import abolt.lcmtypes.observations_t;
import sml.Identifier;

public class WorldTime implements IInputLinkElement
{
    private long startTime = 0;
    
    private long currentTime = 0;
    
    private int stepNumber = 0;
    
    private Identifier timeId;
    
    private boolean hasChanged;
    
    public WorldTime(){
        timeId = null;
        hasChanged = false;
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(timeId == null){
            timeId = parentIdentifier.CreateIdWME("time");
        }
        
        stepNumber++;
        WorkingMemoryUtil.updateIntWME(timeId, "steps", getSteps());
        WorkingMemoryUtil.updateIntWME(timeId, "seconds", (int)getSeconds());
        WorkingMemoryUtil.updateIntWME(timeId, "microseconds", (int)getMicroseconds());
        hasChanged = false;
    }
    
    public synchronized void newObservation(observations_t observation){
        if(startTime == 0){
            startTime = observation.utime;
        }
        currentTime = observation.utime;
        hasChanged = true;
    }

    @Override
    public synchronized void destroy()
    {
        timeId.DestroyWME();
    }
    
    public synchronized int getSteps(){
        return stepNumber;
    }
    
    public synchronized long getSeconds(){
        long seconds = 0;
        if(currentTime != 0){
            seconds = (currentTime - startTime)/1000000;
        }
        return seconds;
    }
    
    public synchronized long getMicroseconds(){
        long microseconds = 0;
        if(currentTime != 0){
            microseconds = currentTime - startTime;
        }
        return microseconds;
    }

}