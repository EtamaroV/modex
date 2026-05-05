package com.modex.modex.datastruct;

public class Parcel {
    private Province from;
    private Province to;
    private Province currentProvince;
    private Edge path;
    private int reward;

    public Parcel(Province From,Province To){
        from = From;
        to = To;
        currentProvince = From;
        path = null;
        reward = 0;
    }

    public Province getFrom() {
        return from;
    }

    public Province getTo() {
        return to;
    }

    public Province getCurrentProvince() {
        return currentProvince;
    }

    public Edge getPath() {
        return path;
    }

    public int getReward() {
        return reward;
    }

    public void setCurrentProvince(Province currentProvince) {
        this.currentProvince = currentProvince;
    }

    public void setPath(Edge path) {
        this.path = path;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }


}
