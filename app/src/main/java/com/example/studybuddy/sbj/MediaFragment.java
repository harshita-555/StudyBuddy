package com.example.studybuddy.sbj;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class MediaFragment extends Fragment {

    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<mediaStore,MediaViewHolder>  mAdapter;

    private StorageReference mStorageReference;
    private static Context mContext;

    private  ImageButton mCamera;
    private ImageButton mVideo;
    private static final int RC_PHOTO_PICKER =1;
    private static final int RC_VIDEO_PICKER=2;
    private String subjectKey;

    private View mMainView;

    public MediaFragment() {    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_media, container, false);
        mConvList = (RecyclerView)  mMainView.findViewById(R.id.list);

        mDatabaseReference.keepSynced(true);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),3);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(gridLayoutManager);

        mCamera=(ImageButton) mMainView.findViewById(R.id.icon_camera);
        mCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        mVideo=(ImageButton) mMainView.findViewById(R.id.icon_video);
        mVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.putExtra(intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent,"Complete action using"),RC_VIDEO_PICKER);
            }
        });



        return mMainView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment()+"/"+  new Date().getTime());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    mediaStore content = new mediaStore(false,downloadUrl.toString());
                    mDatabaseReference.push().setValue(content);


                }

            });
        }

        if (requestCode == RC_VIDEO_PICKER && resultCode == RESULT_OK) {

            Uri selectedVideoUri = data.getData();

            StorageReference photoRef = mStorageReference.child(selectedVideoUri.getLastPathSegment()+"/"+  new Date().getTime());
            photoRef.putFile(selectedVideoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    Toast.makeText(getContext(), "loading...", Toast.LENGTH_SHORT).show();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    mediaStore content = new mediaStore(true,downloadUrl.toString());
                    mDatabaseReference.push().setValue(content);


                }

            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user= FirebaseAuth.getInstance().getCurrentUser();
        String Uid= user.getUid();
        subjectKey=getActivity().getIntent().getStringExtra("SubId");
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Subjects").child(subjectKey).child("media");
        mStorageReference= FirebaseStorage.getInstance().getReference().child("media");
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

        }

        public void setView(mediaStore content) {
            ImageView imgView = mView.findViewById(R.id.media_img);
            String url = content.getMediaUrl();
           if(!content.isVideo()) Glide.with(mContext).load(url).placeholder(R.drawable.img_placeholder).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgView);
           else
               Glide.with(mContext).asBitmap().load(url).placeholder(R.drawable.video_placeholder).thumbnail(0.5f).into(imgView);
        }

        public void showDetail(final DatabaseReference mref, final int position)
        {
           mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent viewIntent= new Intent(getContext(), DetailActivity.class);
                    viewIntent.putExtra("pos", position);
                    viewIntent.putExtra("SubId", subjectKey);
                    startActivity(viewIntent);
                   // getActivity().finish();

                }
            });
        }

        public void setMenu(final DatabaseReference mref)
        {
            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.media_menu, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.share_media:

                                    //yet to written..............................................................................................


                                    return true;
                                case R.id.delete_media:
                                    mref.removeValue();
                                    return true;
                                default:
                                    return false;

                            }
                        }
                    });
                    return false;
                }

            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<mediaStore> options = new FirebaseRecyclerOptions.Builder<mediaStore>().setQuery(mDatabaseReference,mediaStore.class).build();

        mAdapter = new FirebaseRecyclerAdapter<mediaStore, MediaViewHolder>(options) {
            @NonNull
            @Override
            public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false);
                mContext=parent.getContext();
                return new MediaViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MediaViewHolder holder, int position, @NonNull mediaStore model) {

                holder.setView(model);
                holder.showDetail(getRef(position),position);
                holder.setMenu(getRef(position));


            }
        };



        mConvList.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}


