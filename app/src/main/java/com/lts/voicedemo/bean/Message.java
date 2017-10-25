package com.lts.voicedemo.bean;

/**
 * Created Date:  2017/10/25.
 * author: tsliu
 * email: liutangbei@gmail.com
 */

public class Message {
    private String message;
    private int type;

    public Message(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
