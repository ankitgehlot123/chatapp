package com.company.my.chatapp.contactListShow;

public class contactGetSet {
    private String _id, username, mob_no, pic;

    public contactGetSet(String _id, String username, String mob_no, String pic) {
        this.username = username;
        this._id = _id;
        this.mob_no = mob_no;
        this.pic = pic;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return _id;
    }

    public String getMob_no() {
        return mob_no;
    }

    public String getPic() {
        return pic;
    }
}
