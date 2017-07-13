/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

public class StepDistance {
    private int ID;
    private long date;
    private int step;
    private float distance;

    public StepDistance(int ID, long date, int step, float distance) {
        this.ID = ID;
        this.date = date;
        this.step = step;
        this.distance = distance;
    }

    public StepDistance(long date, int step, float distance) {
        this.date = date;
        this.step = step;
        this.distance = distance;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
