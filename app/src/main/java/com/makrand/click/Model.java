package com.makrand.click;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Makrand on 30-09-2017.
 */
@IgnoreExtraProperties
public class Model{
    public String latitude, longitude, id;
    @Exclude public Boolean entered;
    public Model(){}


    public Model(String latitude, String longitude, String id){
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
        this.entered = false;
    }
    public Model(String latitude, String longitude, String id, Boolean entered){
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
        this.entered = false;
    }


    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
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

    @Exclude
    public boolean getEntered(){return entered;}

    @Exclude
    public void setEntered(Boolean val){ this.entered = val;}

}