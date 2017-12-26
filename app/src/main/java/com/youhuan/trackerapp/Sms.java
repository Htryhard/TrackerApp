package com.youhuan.trackerapp;

/**
 * 短信实体类
 * Created by YouHuan on 17/11/7.
 */

public class Sms {
    private int id;
    private String tell;//对方电话号码
    private String content;//内容
    private String type;//发送／接收
    private String time;//发送／接收的时间
    private String nickname;//对方的昵称


    @Override
    public String toString() {
        return "Sms{" +
                "id=" + id +
                ", tell='" + tell + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTell() {
        return tell;
    }

    public void setTell(String tell) {
        this.tell = tell;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}
