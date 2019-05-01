package com.company.my.chatapp.modal;

import android.net.Uri;

import java.util.Date;

public class Message {

    public static final int TYPE_MESSAGE_SENDER = 1;
    public static final int TYPE_MESSAGE_RECEIVER = 2;
    public static final int TYPE_MESSAGE_IMAGE_SENDER = 3;
    public static final int TYPE_MESSAGE_IMAGE_RECEIVER = 4;
    public static final int TYPE_LOG = 5;
    public static final int TYPE_ACTION = 6;
    private int mType;
    private String mMessage;
    private String mUsername;
    private Uri mImage;
    private Date mtimestamp;

    private Message() {
    }

    public int getType() {
        return mType;
    }

    ;

    public String getMessage() {
        return mMessage;
    }

    ;

    public String getUsername() {
        return mUsername;
    }

    ;

    public Uri getImage() {
        return mImage;
    }

    public Date getTimestamp() {
        return mtimestamp;
    }


    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private Uri mImage;
        private Date mTimestamp;
        private int mtrans_type;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder image(Uri image) {
            mImage = image;
            return this;
        }

        public Builder timestamp(Date timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.mtimestamp = mTimestamp;
            message.mImage = mImage;
            return message;
        }
    }
}
