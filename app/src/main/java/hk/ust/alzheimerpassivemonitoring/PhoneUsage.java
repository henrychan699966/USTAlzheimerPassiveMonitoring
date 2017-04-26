package hk.ust.alzheimerpassivemonitoring;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by henry on 2017-04-20.
 */

public class PhoneUsage implements Parcelable {
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

    public PhoneUsage(Parcel in) {
        long[] data = new long[2];
        in.readLongArray(data);
        eventID = in.readInt();
        activity = in.readString();
        startTime = data[0];
        endTime = data[1];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(eventID);
        dest.writeString(activity);
        dest.writeLongArray(new long[] {startTime,endTime});
    }

    public static final Parcelable.Creator<PhoneUsage>CREATOR = new Parcelable.Creator<PhoneUsage>() {
        @Override
        public PhoneUsage createFromParcel(Parcel source) {
            return new PhoneUsage(source);
        }

        @Override
        public PhoneUsage[] newArray(int size) {
            return new PhoneUsage[size];
        }
    };

}
