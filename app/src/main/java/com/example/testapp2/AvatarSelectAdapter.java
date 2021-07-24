package com.example.testapp2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class AvatarSelectAdapter extends RecyclerView.Adapter<AvatarSelectAdapter.MyViewHolder> {
    private List<AvatarModel> avatarList;
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, story;
        ImageView avatarImageView;
        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            story = view.findViewById(R.id.storyText);
            avatarImageView = view.findViewById(R.id.avatarImageView);
        }
    }
    public AvatarSelectAdapter(List<AvatarModel> avatarList) {
        this.avatarList = avatarList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.avatar_list, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AvatarModel avatar = avatarList.get(position);
        holder.name.setText(avatar.getName());
        holder.story.setText(avatar.getStory());
        holder.avatarImageView.setImageURI(avatar.getImageUri());
    }
    @Override
    public int getItemCount() {
        return avatarList.size();
    }
}
