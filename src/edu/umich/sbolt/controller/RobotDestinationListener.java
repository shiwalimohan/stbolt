package edu.umich.sbolt.controller;

public interface RobotDestinationListener
{
    void robotDestinationChanged(double x, double y, double t);
    void setEStop(boolean eStop);
    void setAction(boolean action);
}
