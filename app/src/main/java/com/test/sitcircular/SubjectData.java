package com.test.sitcircular;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;

public class SubjectData implements Parcelable {
    String SubjectName;
    StorageReference Link;
    String Image;

    public SubjectData(String subjectName, StorageReference link, String image) {
        this.SubjectName = subjectName;
        this.Link = link;
        this.Image = image;
    }

    protected SubjectData(Parcel in) {
        SubjectName = in.readString();
        Image = in.readString();
    }

    public static final Creator<SubjectData> CREATOR = new Creator<SubjectData>() {
        @Override
        public SubjectData createFromParcel(Parcel in) {
            return new SubjectData(in);
        }

        @Override
        public SubjectData[] newArray(int size) {
            return new SubjectData[size];
        }
    };

    public String getSubjectName() {
        return SubjectName;
    }

    public StorageReference getLink() {
        return Link;
    }

    public String getImage() {
        return Image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SubjectName);
        dest.writeString(Image);
    }
}
