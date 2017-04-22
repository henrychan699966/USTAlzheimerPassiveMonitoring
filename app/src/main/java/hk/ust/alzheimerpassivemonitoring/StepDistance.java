package hk.ust.alzheimerpassivemonitoring;

/**
 * Created by henry on 2017-04-20.
 */

public class StepDistance {
    private String date;        //YYYYMMDD
    private int step;
    private float distance;


    public StepDistance(String date, int step, float distance) {
        this.date = date;
        this.step = step;
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
