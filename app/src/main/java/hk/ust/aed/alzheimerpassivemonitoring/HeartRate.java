package hk.ust.aed.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-06-15.
 */

public class HeartRate {
    private int ID;

    public HeartRate(int ID, long recordTime, int heartRate) {
        this.ID = ID;
        this.recordTime = recordTime;
        this.heartRate = heartRate;
    }

    private long recordTime;
    private int heartRate;

    public HeartRate(long recordTime, int heartRate) {
        this.recordTime = recordTime;
        this.heartRate = heartRate;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
