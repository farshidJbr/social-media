package com.example.books;

public class findfriends {

    public String fullname,statuse,profileimage;

    public findfriends(){

    }

    public findfriends(String fullname, String statuse, String profileimage) {
        this.fullname = fullname;
        this.statuse = statuse;
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatuse() {
        return statuse;
    }

    public void setStatuse(String statuse) {
        this.statuse = statuse;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
