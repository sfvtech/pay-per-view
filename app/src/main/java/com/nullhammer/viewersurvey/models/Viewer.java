package com.nullhammer.viewersurvey.models;

import java.io.Serializable;

public class Viewer implements Serializable {

    public void setSessionId(Long id) { mSessionId = id; }

    public Long getSessionId() { return mSessionId; }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String Email) {
        this.mEmail = Email;
    }

    public void setSurveyAnswer(String SurveyAnswer) {
        this.mSurveyAnswer = SurveyAnswer;
    }

    public String getSurveyAnswer() {
        return this.mSurveyAnswer;
    }

    private String mName;
    private String mEmail;
    private String mSurveyAnswer;
    private long mSessionId;

    public Viewer(String name, String email, long sessionId) {
        mName = name;
        mEmail = email;
        mSessionId = sessionId;
    }

    @Override
    public String toString() {
        return "Viewer{" +
                "name='" + mName + "'" +
                ", email='" + mEmail + "'" +
                ", answer='" + mSurveyAnswer + "'" +
                ", session_id='" + Long.toString(mSessionId) + "'" +
                '}';
    }
}
