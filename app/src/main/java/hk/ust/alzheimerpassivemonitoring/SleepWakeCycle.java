package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class SleepWakeCycle {
    private long StartTime;
    private long EndTime;
    private String SleepStage;


    public SleepWakeCycle(long startTime, long endTime, String sleepStage) {
        StartTime = startTime;
        EndTime = endTime;
        SleepStage = sleepStage;
    }

    public long getStartTime() {
        return StartTime;
    }

    public void setStartTime(long startTime) {
        StartTime = startTime;
    }

    public long getEndTime() {
        return EndTime;
    }

    public void setEndTime(long endTime) {
        EndTime = endTime;
    }

    public String getSleepStage() {
        return SleepStage;
    }

    public void setSleepStage(String sleepStage) {
        SleepStage = sleepStage;
    }
}
