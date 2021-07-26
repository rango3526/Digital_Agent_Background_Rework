package com.example.testapp2;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp2.ui.avatarSelect.AvatarSelectFragment;

import java.util.List;
public class AvatarSelectAdapter extends RecyclerView.Adapter<AvatarSelectAdapter.MyViewHolder> {
    private List<AvatarModel> avatarList;
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, story;
        ImageView avatarImageView;
        Button selectButton;
        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            story = view.findViewById(R.id.storyText);
            avatarImageView = view.findViewById(R.id.avatarImageView);
            selectButton = view.findViewById(R.id.avatarSelectButton);
        }
    }
    public AvatarSelectAdapter(List<AvatarModel> avatarList) {
        this.avatarList = avatarList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.avatar_list_item, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AvatarModel avatar = avatarList.get(position);
        holder.name.setText(avatar.getName());
        holder.story.setText(avatar.getStory());
        holder.avatarImageView.setImageURI(avatar.getImageUri());
        holder.selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarSelectFragment.setCurAvatar(v.getContext(), avatar.getName());
                Toast.makeText(v.getContext(), "Your avatar has been set", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return avatarList.size();
    }
}
