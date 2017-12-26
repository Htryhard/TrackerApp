package com.youhuan.trackerapp;

/**
 * 电话实体类
 * Created by YouHuan on 17/11/7.
 */

public class Call {
    private int id;
    private String tell;//对方电话号码
    private String type;//类型：已接，未接
    private String time;//来电／拨打时间
    private String duration;//通话时长
    private String nickname;//对方的昵称

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTell() {
        return tell;
    }

    public void setTell(String tell) {
        this.tell = tell;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Call{" +
                "id=" + id +
                ", tell='" + tell + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", duration='" + duration + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
