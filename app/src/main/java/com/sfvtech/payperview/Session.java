package com.sfvtech.payperview;

import java.util.Date;

public class Session {
    private long mSessionId;
    private Date mStartTime;
    private Date mEndTime;

    public Session() {
        mStartTime = new Date();
    }

    public void setEndTime(Date time) {
        mEndTime = time;
    }

    public long getId() {
        return mSessionId;
    }

    public void setId(long id) {
        mSessionId = id;
    }
}
