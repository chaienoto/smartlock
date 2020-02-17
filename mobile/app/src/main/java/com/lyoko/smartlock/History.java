package com.lyoko.smartlock;

import android.widget.ImageView;

public class History {
    private String Title, Description;
    private int Ic;

    public History(){

    }

    public History(String Title, String Description, int ic) {
        this.Title = Title;
        this.Description = Description;
        this.Ic = Ic;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public int getIc() {
        return Ic;
    }

    public void setIc(int Ic) {
        this.Ic = Ic;
    }



}
