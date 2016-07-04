package me.lab.photonics.photonicslab;

public class DayData {
    private String timeStamp, data;

    public DayData(String timeStamp, String data) {
        this.timeStamp = timeStamp;
        this.data = data;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
