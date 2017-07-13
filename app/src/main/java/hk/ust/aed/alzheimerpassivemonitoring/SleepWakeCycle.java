/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

public class SleepWakeCycle {
    private int ID;
    private long startTime;
    private long endTime;
    private String sleepStage;


    public SleepWakeCycle(long startTime, long endTime, String sleepStage) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.sleepStage = sleepStage;
    }

    public SleepWakeCycle(int ID, long startTime, long endTime, String sleepStage) {
        this.ID = ID;
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

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
