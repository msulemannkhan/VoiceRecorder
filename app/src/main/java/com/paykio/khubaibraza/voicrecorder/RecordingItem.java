package com.paykio.khubaibraza.voicrecorder;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Created by Daniel on 12/30/2014.
 */
public class RecordingItem implements Parcelable,Comparable<RecordingItem> {
    private String mName; // file name
    private String mFilePath; //file path
    private int mId; //id in database
    private int mLength; // length of recording in seconds
    private long mTime; // date/time of the recording
    private String mType;
    private int mSize;

    public RecordingItem()
    {
    }

    public RecordingItem(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
        mId = in.readInt();
        mLength = in.readInt();
        mTime = in.readLong();
        mType = in.readString();
        mSize = in.readInt();
    }

    public String getFileType() { return mType; }

    public void setFileType(String mType) {
        this.mType = mType;
    }

    public int getFileSize() {
        return mSize;
    }

    public void setFileSize(int mSize) {
        this.mSize = mSize;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public static final Parcelable.Creator<RecordingItem> CREATOR = new Parcelable.Creator<RecordingItem>() {
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mLength);
        dest.writeLong(mTime);
        dest.writeString(mFilePath);
        dest.writeString(mName);
        dest.writeString(mType);
        dest.writeInt(mSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int compareTo(@NonNull RecordingItem o) {
        int last = this.mName.compareTo(o.mName);
        return last == 0 ? this.mName.compareTo(o.mName) : last;

    }

}