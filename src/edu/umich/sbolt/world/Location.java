package edu.umich.sbolt.world;

/**
 * A class for describing locations in the world using x, y, theta
 * 
 * @author mininger
 * 
 */
public class Location
{
    public double x = 0;
    public double y = 0;
    public double t = 0;
    
    // Sets the location to the given one (copy)
    public void set(Location l){
        x = l.x;
        y = l.y;
        t = l.t;
    }
    
    // Returns the distance between two locations
    public double dist(Location l){
        return Math.sqrt((l.x - x)*(l.x - x) + (l.y - y)*(l.y - y));
    }
    
    // Returns the distance squared between the two locations
    public double dist2(Location l){
        return (l.x - x)*(l.x - x) + (l.y - y)*(l.y - y);
    }
    
    // Returns the angle between the two thetas
    // (From 0 to Pi)
    public double dTheta(Location l){
        double dTheta = Math.abs(t - l.t);
        if(dTheta > Math.PI){
            dTheta = Math.abs(dTheta - 2 * Math.PI);
        }
        return dTheta;
    }
}
