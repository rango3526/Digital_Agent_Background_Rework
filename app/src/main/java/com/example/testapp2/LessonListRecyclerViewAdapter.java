package com.example.testapp2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp2.ui.gallery.GalleryFragment;
import com.example.testapp2.ui.lesson.LessonFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LessonListRecyclerViewAdapter extends RecyclerView.Adapter<LessonListRecyclerViewAdapter.ViewHolder> {

//    ArrayList<String> objectNames = new ArrayList<>();
//    ArrayList<String> imagesUriStrings = new ArrayList<>();
    ArrayList<MyImage> myImages = new ArrayList<>();
    Context context;
    FragmentManager fragmentManager;

    public LessonListRecyclerViewAdapter(ArrayList<MyImage> myImages, FragmentManager fragmentManager, Context context) {
//        this.objectNames = objectNames;
//        this.imagesUriStrings = imagesUriStrings;
        this.myImages = myImages;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_image_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.w("Stuff", "onBindViewHolder called");

        MyImage curImage = myImages.get(position);

//        Log.w("Stuff", "Before uri string: " + String.join(", ", imagesUriStrings));
        String uriString = curImage.uriString;
        Log.w("Stuff", "uriString: " + uriString);

        holder.imageView.setImageURI(Uri.parse(uriString));
        holder.objectName.setText(curImage.objectDetected);
        holder.bookmarkToggle.setChecked(curImage.bookmarked);

        holder.bookmarkToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GalleryFragment.setImageBookmark(context, curImage.imageID, isChecked);
            }
        });

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.w("Stuff", "onClick, clicked on " + curImage.objectDetected);
//                Intent intent = HelperCode.getIntentForObjectLesson(context, curImage);
//                context.startActivity(intent);
                // ^^^ Old

                LessonFragment lessonFragment = new LessonFragment();
                lessonFragment.setLessonData(curImage);
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main_layout, lessonFragment, "fragmentTag")
                        .addToBackStack(null)
                        .commit();

//                fragmentManager.setFragmentResultListener("requestKey", context, new FragmentResultListener() {
//                    @Override
//                    public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
//                        Log.w("FragmentStuff", "Got the result: " + result.toString());
//                    }
//                });

//                Bundle result = new Bundle();
//                result.putString("bundleKey", "myyyyResult");
//                fragmentManager.setFragmentResult("requestKey", result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView objectName;
        ToggleButton bookmarkToggle;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.objectImageView);
            objectName = itemView.findViewById(R.id.objectTextView);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            bookmarkToggle = itemView.findViewById(R.id.bookmarkToggle);
            //TODO: set values of those fields (with binding stuff)
        }
    }
}
