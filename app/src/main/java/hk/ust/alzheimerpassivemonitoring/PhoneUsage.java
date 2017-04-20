package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class PhoneUsage {
    private int EventID;
    private String Activity;
    private long StartTime;
    private long EndTime;

    public PhoneUsage(String a, long s, long e){
        this.Activity = a;
        this.StartTime = s;
        this.EndTime = e;
    }

    public PhoneUsage(int id, String a, long s, long e){
        this.EventID = id;
        this.Activity = a;
        this.StartTime = s;
        this.EndTime = e;
    }


    public int getEventID() {
        return EventID;
    }

    public String getActivity() {
        return Activity;
    }

    public long getStartTime() {
        return StartTime;
    }

    public long getEndTime() {
        return EndTime;
    }

    public void setEventID(int eventID) {
        EventID = eventID;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }

    public void setStartTime(long startTime) {
        StartTime = startTime;
    }

    public void setEndTime(long endTime) {
        EndTime = endTime;
    }
}
