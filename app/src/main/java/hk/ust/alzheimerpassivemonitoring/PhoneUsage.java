package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class PhoneUsage {
    private int eventID;
    private String activity;
    private long startTime;
    private long endTime;

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
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

    public PhoneUsage(int eventID, String activity, long startTime, long endTime) {
        this.eventID = eventID;
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
