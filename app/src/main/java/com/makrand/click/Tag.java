package com.makrand.click;

/**
 * Created by Makrand on 03-10-2017.
 */

public class Tag {
    private String id, name, licence;

    Tag() {
    }

    Tag(String id, String name, String licence) {
        this.id = id;
        this.name = name;
        this.licence = licence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    String getId(){
        return id;
    }

    void setId(String id){
        this.id = id;
    }

}