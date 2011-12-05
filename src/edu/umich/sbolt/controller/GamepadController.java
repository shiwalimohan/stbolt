package edu.umich.sbolt.controller;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import lcm.lcm.LCM;
import april.lcmtypes.gamepad_t;
import april.util.TimeUtil;

/**
 * Takes higher-level commands and spits out gamepad_t over LCM.
 * 
 * @author miller
 * 
 */
public class GamepadController implements RobotPositionListener, RobotDestinationListener
{
    private LCM lcm = LCM.getSingleton();
    private double[] speed = new double[2]; // rotation, acceleration
    private boolean eStop = false;
    private double[] location = new double[3]; // xyt
    private double[] target = null; // xyt

    private Runnable lcmRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                TimeUtil.sleep(100);
                gamepad_t gp = new gamepad_t();
                gp.naxes = 6; // for compatability -- we only use the last two
                gp.axes = new double[gp.naxes];
                gp.present = true;
                synchronized (this)
                {
                    gp.utime = TimeUtil.utime();
                    gp.axes[4] = speed[0];
                    gp.axes[5] = speed[1];
                    gp.buttons = eStop || target == null ? 0 : 1;
                }
                lcm.publish("GAMEPAD", gp);
            }
        }
    };
    
    public GamepadController(double x, double y, double theta)
    {
        location[0] = x;
        location[1] = y;
        location[2] = theta;
    }
    
    private double deltaTheta(double from, double to) {
        double ret = to - from;
        if (ret < Math.PI) ret += 2.0 * Math.PI;
        if (ret > Math.PI) ret -= 2.0 * Math.PI;
        return ret;
    }
    
    private static double TARGET_DISTANCE_THRESHOLD = 0.1;
    private static double TARGET_ANGLE_THRESHOLD = 0.1;
    private static double TARGET_MOVING_ANGLE_THRESHOLD = 0.2;

    public void robotPositionChanged(double x, double y, double t)
    {
        synchronized (this)
        {
            location[0] = x;
            location[1] = y;
            location[2] = t;
            if (target == null) return;
            
            // Update speed to get toward the target.
            // Base case 1: if we're close enough to the target x/y,
            // just adjust out angle to get to the target angle.
            double dx = target[0] - location[0];
            double dy = target[1] - location[1];
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < TARGET_DISTANCE_THRESHOLD)
            {
                speed[1] = 0.0;
                double dt = deltaTheta(location[2], target[2]);
                if (Math.abs(dt) < TARGET_ANGLE_THRESHOLD)
                {
                    speed[0] = 0.0;
                    target = null;
                    return;
                }
                speed[0] = -dt / 2.0;
                if (speed[0] < -1.0) speed[0] = -1.0;
                if (speed[0] > 1.0) speed[0] = 1.0;
                return;
            }
            
            // Case 2: We're pointing toward the target. Move forward.
            double targetTheta = Math.atan2(dy, dx);
            double dt = deltaTheta(location[2], targetTheta);
            System.out.println("Target theta: " + targetTheta + ", location: " + location[2] + ", dt: " + dt + ", diff: " + (targetTheta - location[2]));
            if (Math.abs(dt) < TARGET_MOVING_ANGLE_THRESHOLD)
            {
                speed[0] = -dt / 2.0;
                if (speed[0] < -1.0) speed[0] = -1.0;
                if (speed[0] > 1.0) speed[0] = 1.0;
                speed[1] = -1.0 + Math.abs(speed[0]);
                return;
            }
            
            // Case 3: just rotate to face the target.
            speed[0] = -dt / 2.0;
            if (speed[0] < -1.0) speed[0] = -1.0;
            if (speed[0] > 1.0) speed[0] = 1.0;
            speed[1] = 0.0;
            return;
        }
    }

    public void setEStop(boolean eStop)
    {
        synchronized (this)
        {
            this.eStop = eStop;
        }
    }

    public void setRotationSpeed(double rotation)
    {
        synchronized (this)
        {
            target = null;
            this.speed[0] = rotation;
        }
    }

    public void setAccelerationSpeed(double acceleration)
    {
        synchronized (this)
        {
            target = null;
            this.speed[1] = acceleration;
        }
    }
    
    public void robotDestinationChanged(double x, double y, double t)
    {
        synchronized (this)
        {
            target = new double[] {x, y, t};
        }
    }

    public void start()
    {
        new Thread(lcmRunnable).start();
    }
}
