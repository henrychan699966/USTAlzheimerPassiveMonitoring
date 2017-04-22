package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class SleepWakeCycle {
    private long startTime;
    private long endTime;
    private String sleepStage;


    public SleepWakeCycle(long startTime, long endTime, String sleepStage) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.sleepStage = sleepStage;
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

    public String getSleepStage() {
        return sleepStage;
    }

    public void setSleepStage(String sleepStage) {
        this.sleepStage = sleepStage;
    }
}
