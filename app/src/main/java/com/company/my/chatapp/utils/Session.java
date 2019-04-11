package com.company.my.chatapp.utils;


import android.content.Context;

public class Session {
    static public String userId = "userId";
    static public String mob_no = "mob_no";
    static public String username = "username";
    static public String profilePic = "profilePic";



    static public String regisToken = "regisToken";

    Context context;

    public Session(Context context) {
        this.context = context;
    }

    public void ClearSession() {
        utils.setShared(context, userId, "");
        utils.setShared(context, mob_no, "");
        utils.setShared(context, username, "");
        utils.setShared(context, profilePic, "");
    }

    public String getUserId() {
        return utils.getShared(context, userId, "");
    }

    public void setUserId(String Id) {
        utils.setShared(context, userId, Id);
    }

    public String getMob_no() {
        return utils.getShared(context, mob_no, "");
    }

    public void setMob_no(String mobNo) {
        utils.setShared(context, mob_no, mobNo);
    }

    public String getUsername() {
        return utils.getShared(context, username, "");
    }

    public void setUsername(String userName) {
        utils.setShared(context, username, userName);
    }

    public String getProfilePic() { return utils.getShared(context, profilePic, ""); }

    public void setProfilePic(String pic) { utils.setShared(context, profilePic, pic); }

    public String getRegisToken() { return utils.getShared(context,regisToken, ""); }

    public void setRegisToken(String token) { utils.setShared(context, regisToken, token); }

}
