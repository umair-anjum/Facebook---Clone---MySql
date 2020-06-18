package com.example.smalldots.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smalldots.Activities.FullPostActivity;
import com.example.smalldots.R;
import com.example.smalldots.model.PostModel;
import com.example.smalldots.rest.ApiClient;
import com.example.smalldots.utils.AgoDateParse;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context context;
    List<PostModel> postModels;

    public PostAdapter(Context context, List<PostModel> postModels) {
        this.context = context;
        this.postModels = postModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final PostModel postModel = postModels.get(position);
        if (postModel.getPost() != null && postModel.getPost().length() > 1) {
            holder.post.setText(postModel.getPost());
        } else {
            holder.post.setVisibility(View.GONE);
        }
        holder.peopleName.setText(postModel.getName());
        if (postModel.getPrivacy().equals("0")) {
            holder.privacyIcon.setImageResource(R.drawable.icon_friends);
        } else if (postModel.getPrivacy().equals("1")) {
            holder.privacyIcon.setImageResource(R.drawable.icon_onlyme);
        } else {
            holder.privacyIcon.setImageResource(R.drawable.icon_public);
        }
        if (!postModel.getStatusImage().isEmpty()) {

            Picasso.with(context).load(ApiClient.BASE_URL_1+postModel.getStatusImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(holder.statusImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Toast.makeText(context, "picture in", Toast.LENGTH_SHORT).show();
                    Picasso.with(context).load(ApiClient.BASE_URL_1+postModel.getStatusImage()).placeholder(R.drawable.default_image_placeholder).into(holder.statusImage);
                }
            });
        }else{
            holder.statusImage.setImageDrawable(null);
        }

        try {
            holder.date.setText(AgoDateParse.getTimeAgo(AgoDateParse.getTimeInMillsecond(postModel.getStatusTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(!postModel.getProfileUrl().isEmpty()){
            Picasso.with(context).load(postModel.getProfileUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(holder.peopleImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(postModel.getProfileUrl()).placeholder(R.drawable.default_image_placeholder).into(holder.peopleImage);
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullPostActivity.class);
                Bundle bundle = new Bundle();
                // bundle.putParcelable("postModel", Parcels.wrap(postModel));
                intent.putExtra("postBundle", bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.people_image)
        ImageView peopleImage;
        @BindView(R.id.people_name)
        TextView peopleName;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.privacy_icon)
        ImageView privacyIcon;
        @BindView(R.id.memory_meta_rel)
        RelativeLayout memoryMetaRel;
        @BindView(R.id.post)
        TextView post;
        @BindView(R.id.like_img)
        ImageView likeImg;
        @BindView(R.id.status_image)
        ImageView statusImage;
        @BindView(R.id.like_txt)
        TextView likeTxt;
        @BindView(R.id.likeSection)
        LinearLayout likeSection;
        @BindView(R.id.comment_img)
        ImageView commentImg;
        @BindView(R.id.comment_txt)
        TextView commentTxt;
        @BindView(R.id.commentSection)
        LinearLayout commentSection;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
