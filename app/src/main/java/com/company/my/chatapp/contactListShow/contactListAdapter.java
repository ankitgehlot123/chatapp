package com.company.my.chatapp.contactListShow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.company.my.chatapp.MainFragment;
import com.company.my.chatapp.R;
import com.company.my.chatapp.chat_base;
import com.company.my.chatapp.utils.MyVolleySingelton;
import com.company.my.chatapp.utils.utils;

import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class contactListAdapter extends RecyclerView.Adapter{

    private List<contactGetSet> contactList;
    private Context context;
    private LayoutInflater inflater;


    boolean value;


    public contactListAdapter(Context context, List<contactGetSet> contactList) {
        this.context = context;
        this.contactList = contactList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.contact_list_view, parent, false);

            return new contactListAdapter.MyViewHolder(v);
        }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


            contactGetSet feeds = contactList.get(position);
            //Pass the values of feeds object to Views
            ((MyViewHolder)holder).username.setText(feeds.getUsername());
            ((MyViewHolder)holder).mob_no = feeds.getMob_no();
            //Set Image If Already exist
            if (feeds.getPic()!="profilepic.jpeg" && feeds.getPic()!=null){
                //Log.d("ImageDB",StringToBitMap(feeds.getPic()).toString());
                byte[] decodedString = Base64.decode(feeds.getPic(), Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ((MyViewHolder) holder).contact_image.setImageBitmap(decodeByte);
            }


            String url = utils.profile_url + feeds.getId();
            ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if(bitmap!=null) {
                            ((MyViewHolder) holder).contact_image.setImageBitmap(bitmap);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                            Document filter = new Document();
                            filter.put("_id",feeds.getId());
                            Document field = new Document();
                            field.put("pic",imageString);
                            Document update = new Document();
                            update.put("$set",field);
                            utils.contactListCollection.updateOne(filter,update);

                        }
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            // Access the RequestQueue through your singleton class.
                MyVolleySingelton.getInstance(context).addToRequestQueue(request);
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void clear() {
        final int size = contactList.size();
        contactList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        ImageView contact_image;
        String mob_no;
        public MyViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.contact_name);
            contact_image = itemView.findViewById(R.id.contact_image);

            itemView.setOnClickListener(view -> {
            Intent intent =new Intent(context,chat_base.class);
                intent.putExtra("username",username.getText());
                intent.putExtra("mob_no",mob_no);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            });
        }
    }


}
