package com.example.studybuddy.sbj;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class DocsFragment extends Fragment {

    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<docStore, DocsFragment.DocViewHolder>  mAdapter;
    private StorageReference mStorageReference;
    private  ImageButton mDocs;
    private static final int RC_DOC_PICKER=2;
    private String subjectKey;
    private View mMainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_docs, container, false);
        mConvList = (RecyclerView)  mMainView.findViewById(R.id.list_docs);

        mDatabaseReference.keepSynced(true);


        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mDocs=(ImageButton) mMainView.findViewById(R.id.icon_docs);

        mDocs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String[] mimeTypes =
                        {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.google-apps.document", // .doc & .docx
                                "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                                "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                                "text/plain","application/pdf","application/zip","application/vnd.google-apps.file"};

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType( "*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(Intent.createChooser(intent,"ChooseFile"), RC_DOC_PICKER);


            }
        });

        return mMainView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_DOC_PICKER && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String mimeType = getActivity().getContentResolver().getType(uri);

            Cursor returnCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            Long size = returnCursor.getLong(sizeIndex);


            final docStore doc1 = new docStore();
            if(name!=null|| !name.equals(""))doc1.setDocName(name);
            doc1.setDocSize(size);
            doc1.setDocType(mimeType);

            StorageReference photoRef = mStorageReference.child(uri.getLastPathSegment()+"/"+  new Date().getTime());
            photoRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    Toast.makeText(getContext(), "loading...", Toast.LENGTH_SHORT);
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();

                    doc1.setDocUrl( downloadUrl.toString());
                    mDatabaseReference.push().setValue(doc1);


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
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Subjects").child(subjectKey).child("docs");
        mStorageReference= FirebaseStorage.getInstance().getReference().child("docs");
    }

    public class DocViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

        }
        public  String convertorSize(long size) {
            long n = 1000;
            String s = "";
            double kb = size / n;
            double mb = kb / n;
            double gb = mb / n;
            double tb = gb / n;
            if(size < n) {
                s = size + " Bytes";
            } else if(size >= n && size < (n * n)) {
                s =  String.format("%.2f", kb) + " KB";
            } else if(size >= (n * n) && size < (n * n * n)) {
                s = String.format("%.2f", mb) + " MB";
            } else if(size >= (n * n * n) && size < (n * n * n * n)) {
                s = String.format("%.2f", gb) + " GB";
            } else if(size >= (n * n * n * n)) {
                s = String.format("%.2f", tb) + " TB";
            }
            return s;
        }

        public void setView(final docStore doc1)
        {
            String extension;
            ImageView docType =(ImageView) mView.findViewById(R.id.doc_type);
            if(doc1.getDocType().equals("application/pdf")) {docType.setImageResource(R.drawable.pdf_32); extension="PDF";}

            else if(doc1.getDocType().equals("application/vnd.ms-powerpoint")|| doc1.getDocType().equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                docType.setImageResource(R.drawable.ppt_32);
                extension="PPT";
            }
            else
            {
                docType.setImageResource(R.drawable.doc_32); extension="DOC";
            }



            TextView nameView=(TextView) mView.findViewById(R.id.doc_name);
            nameView.setText(doc1.getDocName());

            TextView detailView =(TextView) mView.findViewById(R.id.doc_details);
            detailView.setText(convertorSize(doc1.getDocSize())+" . "+ extension);

            LinearLayout mLayout= (LinearLayout) mView.findViewById(R.id.doc_text_layout);
            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent viewDoc=new Intent(Intent.ACTION_VIEW);
                   viewDoc.setDataAndType(Uri.parse(doc1.getDocUrl()), doc1.getDocType());
                    if (viewDoc.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(viewDoc);
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "Do not have app to view it", Toast.LENGTH_SHORT).show();
                    }
                }
            });



        }



        public void setMenu(final DatabaseReference mref)
        {
            ImageButton menubtn=(ImageButton) mView.findViewById(R.id.doc_menu);
            menubtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.menu_docs, popup.getMenu());
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

                                case R.id.rename_media:
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                    alertDialog.setTitle("Rename");
                                    alertDialog.setMessage("Enter new name :");

                                    final EditText input = new EditText(getContext());
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    input.setLayoutParams(lp);
                                    alertDialog.setView(input);

                                    alertDialog.setPositiveButton("RENAME",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                   String password = input.getText().toString();
                                                    if (!password.equals("")) {
                                                        mref.child("docName").setValue(password);

                                                        } else {
                                                            Toast.makeText(getContext(),
                                                                    "Enter text!", Toast.LENGTH_SHORT).show();
                                                        }

                                                }
                                            });

                                    alertDialog.setNegativeButton("CANCEL",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });

                                    alertDialog.show();
                                    return true;
                                default:
                                    return false;

                            }
                        }
                    });
                    return ;
                }

            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<docStore> options = new FirebaseRecyclerOptions.Builder<docStore>().setQuery(mDatabaseReference,docStore.class).build();

        mAdapter = new FirebaseRecyclerAdapter<docStore, DocsFragment.DocViewHolder>(options) {
            @NonNull
            @Override
            public DocsFragment.DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_list_item, parent, false);
                return new DocsFragment.DocViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DocsFragment.DocViewHolder holder, int position, @NonNull docStore model) {

                holder.setView(model);
               // holder.showDetail(getRef(position),position);
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
