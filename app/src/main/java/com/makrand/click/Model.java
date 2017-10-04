package com.makrand.click;

/**
 * Created by Makrand on 30-09-2017.
 */

public class Model{
    String latitude, longitude, id;
    public Model(){}


    public Model(String latitude, String longitude, String id){
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;

    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}