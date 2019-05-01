package com.company.my.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.my.chatapp.R;
import com.company.my.chatapp.imageFullscreen;
import com.company.my.chatapp.modal.Message;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class MessageAdapterGroup extends RecyclerView.Adapter<MessageAdapterGroup.ViewHolder> {
    Context context;
    private List<Message> mMessages;
    private int[] mUsernameColors;


    public MessageAdapterGroup(Context context, List<Message> messages) {
        mMessages = messages;
        this.context = context;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_MESSAGE_SENDER:
                layout = R.layout.item_message_sender;
                Log.i("Ankitlayout", "R.layout.item_message_sender");
                break;
            case Message.TYPE_MESSAGE_RECEIVER:
                layout = R.layout.item_message_receiver;
                Log.i("Ankitlayout", "R.layout.item_message_receiver");
                break;
            case Message.TYPE_MESSAGE_IMAGE_SENDER:
                layout = R.layout.item_message_image_sender;
                Log.i("Ankitlayout", "R.layout.item_message_sender");
                break;
            case Message.TYPE_MESSAGE_IMAGE_RECEIVER:
                layout = R.layout.item_message_image_receiver;
                Log.i("Ankitlayout", "R.layout.item_message_receiver");
                break;
            case Message.TYPE_LOG:
                Log.i("TansType", "Log");
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                Log.i("TansType", "action");
                layout = R.layout.item_action_group;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsername());
        try {
            viewHolder.setImage(message.getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewHolder.setmTimeStamp(message.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;
        private SimpleDraweeView mImageView;
        private TextView mTimeStamp;


        public ViewHolder(View itemView) {
            super(itemView);
            mTimeStamp = itemView.findViewById(R.id.time_stamp);
            mUsernameView = itemView.findViewById(R.id.username);
            mMessageView = itemView.findViewById(R.id.message);
            mImageView = itemView.findViewById(R.id.image);


        }

        public void setImage(final Uri uri) throws IOException {
            if (null == mImageView) return;
            if (null == uri) return;
            mImageView.setVisibility(View.VISIBLE);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, imageFullscreen.class);
                    intent.putExtra("image", uri.toString());

                    context.startActivity(intent);
                }
            });
            setPic(uri);
        }

        private void setPic(Uri uri) {

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setProgressiveRenderingEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(mImageView.getController())
                    .build();
            mImageView.setController(controller);


           /* Log.i("ANKIT_URL", uri.toString() + "\n" + uri.getEncodedPath());
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            int targetW, targetH;
            targetW = 300;
            targetH = 300;
            mImageView.getLayoutParams().height = targetH;
            mImageView.getLayoutParams().width = targetW;
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            ;
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            Bitmap b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
            mImageView.setImageBitmap(b);*/

        }

        private void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setVisibility(View.VISIBLE);
            mUsernameView.setText(username);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(message);
        }

        private void setmTimeStamp(Date timestamp) {
            if (null == mTimeStamp) return;
            mTimeStamp.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp.getTime()));
        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }

    }

}
