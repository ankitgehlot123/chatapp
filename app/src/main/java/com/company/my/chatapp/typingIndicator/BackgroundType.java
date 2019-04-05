package com.company.my.chatapp.typingIndicator;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef(value = {BackgroundType.ROUNDED, BackgroundType.SQUARE})
@Retention(RetentionPolicy.SOURCE)
public @interface BackgroundType {
    int ROUNDED = 1;
    int SQUARE = 0;
}