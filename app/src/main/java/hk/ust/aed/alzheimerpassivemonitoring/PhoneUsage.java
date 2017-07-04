/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

import android.os.Parcel;
import android.os.Parcelable;

public class PhoneUsage {
    private int ID;
    private String activity;
    private long startTime;
    private long endTime;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public PhoneUsage(int ID, String activity, long startTime, long endTime) {
        this.ID = ID;
        this.activity = activity;

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public PhoneUsage(String activity, long startTime, long endTime) {
        this.activity = activity;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
