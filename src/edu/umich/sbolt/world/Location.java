package edu.umich.sbolt.world;

public class Location
{
    public double x = 0;
    public double y = 0;
    public double t = 0;
    
    public void set(Location l){
        x = l.x;
        y = l.y;
        t = l.t;
    }
    
    public double dist(Location l){
        return Math.sqrt((l.x - x)*(l.x - x) + (l.y - y)*(l.y - y));
    }
    
    public double dist2(Location l){
        return (l.x - x)*(l.x - x) + (l.y - y)*(l.y - y);
    }
}
