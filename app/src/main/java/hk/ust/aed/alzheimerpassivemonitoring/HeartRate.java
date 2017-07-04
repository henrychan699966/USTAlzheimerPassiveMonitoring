package hk.ust.aed.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-06-15.
 */

public class HeartRate {
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
}
