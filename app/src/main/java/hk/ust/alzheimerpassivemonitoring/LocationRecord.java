package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class LocationRecord {
    private long RecordTime;
    private float Latitude;
    private float Longitude;


    public LocationRecord(long recordTime, float latitude, float longitude) {
        RecordTime = recordTime;
        Latitude = latitude;
        Longitude = longitude;
    }


    public long getRecordTime() {
        return RecordTime;
    }

    public void setRecordTime(long recordTime) {
        RecordTime = recordTime;
    }

    public float getLatitude() {
        return Latitude;
    }

    public void setLatitude(float latitude) {
        Latitude = latitude;
    }

    public float getLongitude() {
        return Longitude;
    }

    public void setLongitude(float longitude) {
        Longitude = longitude;
    }
}
