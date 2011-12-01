package edu.umich.sbolt;

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
public class GamepadController
{
    private LCM lcm = LCM.getSingleton();
    private double rotationSpeed = 0.0;
    private double accelerationSpeed = 0.0;
    private boolean eStop = false;
    private Point2D location;
    private double theta;

    private Runnable lcmRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                TimeUtil.sleep(50);
                gamepad_t gp = new gamepad_t();
                gp.naxes = 6; // for compatability -- we only use the last two
                gp.axes = new double[gp.naxes];
                gp.present = true;
                synchronized (this)
                {
                    gp.utime = TimeUtil.utime();
                    gp.axes[4] = rotationSpeed;
                    gp.axes[5] = accelerationSpeed;
                    gp.buttons = eStop ? 0 : 1;
                }
                lcm.publish("GAMEPAD", gp);
            }
        }
    };

    public GamepadController(double x, double y, double theta)
    {
        location = new Point2D.Double(x, y);
        this.theta = theta;
        new Thread(lcmRunnable).start();
    }

    public void updateLocation(double x, double y, double theta)
    {
        synchronized (this)
        {
            location.setLocation(x, y);
            this.theta = theta;
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
            this.rotationSpeed = rotation;
        }
    }

    public void setAccelerationSpeed(double acceleration)
    {
        synchronized (this)
        {
            this.accelerationSpeed = acceleration;
        }
    }
}
