package com.newland.sunpower.db;

import org.litepal.crud.DataSupport;

public class Result extends DataSupport {
    private double AngleX;
    private double AngleY;
    private double Light;
    private boolean LampStatus;

    public double getAngleX(){return AngleX;}
    public void setAngleX(double AngleX){this.AngleX=AngleX;}

    public double getAngleY(){return AngleY;}
    public void setAngleY(double AngleY){this.AngleY=AngleY;}

    public double getLight(){return Light;}
    public void setLight(double Light){this.Light=Light;}

    public boolean getLampStatus(){return LampStatus;}
    public void setLampStatus(){this.LampStatus=LampStatus;}
}
