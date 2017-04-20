package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class StepDistance {
    private String Date;        //YYYYMMDD
    private int Step;
    private float Distance;

    public StepDistance(String date, int step, float distance) {
        Date = date;
        Step = step;
        Distance = distance;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getStep() {
        return Step;
    }

    public void setStep(int step) {
        Step = step;
    }

    public float getDistance() {
        return Distance;
    }

    public void setDistance(float distance) {
        Distance = distance;
    }
}
