package com.sfvtech.payperview;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Viewer implements Serializable, Parcelable {

    public static final Creator<Viewer> CREATOR = new Creator<Viewer>() {
        @Override
        public Viewer createFromParcel(Parcel in) {
            return new Viewer(in);
        }

        @Override
        public Viewer[] newArray(int size) {
            return new Viewer[size];
        }
    };
    private String mName;
    private String mEmail;
    private String mSurveyAnswer;
    private long mSessionId;

    public Viewer(String name, String email, long sessionId) {
        mName = name;
        mEmail = email;
        mSessionId = sessionId;
    }

    protected Viewer(Parcel in) {
        mName = in.readString();
        mEmail = in.readString();
        mSurveyAnswer = in.readString();
        mSessionId = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mEmail);
        parcel.writeString(mSurveyAnswer);
        parcel.writeLong(mSessionId);
    }

    public Long getSessionId() {
        return mSessionId;
    }

    public void setSessionId(Long id) {
        mSessionId = id;
    }

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

    public String getSurveyAnswer() {
        return this.mSurveyAnswer;
    }

    public void setSurveyAnswer(String SurveyAnswer) {
        this.mSurveyAnswer = SurveyAnswer;
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
