package com.test.sitcircular;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.sitcircular.util.Helper;

import java.io.InputStream;
import java.util.ArrayList;

public class List extends AppCompatActivity {
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private CustomAdapter customAdapter;
    private ArrayList<SubjectData> arrayList;
    private ArrayList<String> children;
    private FloatingActionButton floatingActionButton, folder, file,add_file;
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;
    private boolean isfabOPen = false;
    private UploadTask mUploadTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    @SuppressLint({"RestrictedApi", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        children = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        floatingActionButton = findViewById(R.id.fab);
        folder = findViewById(R.id.folder);
        file = findViewById(R.id.file);
        add_file = findViewById(R.id.add_file);
        swipeRefreshLayout=findViewById(R.id.swipe);
        floatingActionButton.setVisibility(View.GONE);
        add_file.setVisibility(View.GONE);
        file.setVisibility(View.GONE);
        folder.setVisibility(View.GONE);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Admins");
        ValueEventListener eventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String val = d.getValue(String.class);

                    if (val.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        floatingActionButton.setVisibility(View.VISIBLE);
                        add_file.setVisibility(View.VISIBLE);
                        file.setVisibility(View.VISIBLE);
                        folder.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListener);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        Intent i = getIntent();
        Bundle b = i.getExtras();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        if (b != null) {
            children = (ArrayList<String>) i.getBundleExtra("b").getSerializable("ch");
            for (String ch : children)
                storageRef = storageRef.child(ch);
            //subjectData = i.getBundleExtra("b").getParcelable("link");
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isfabOPen) {
                    showFab();
                } else
                    closeFab();
            }
        });
        add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent=Intent.createChooser(intent,"Choose a file: ");

                        startActivityForResult(intent, 1);
            }
        });
        listView= findViewById(R.id.list);
        registerForContextMenu(listView);

        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater=LayoutInflater.from(List.this);
                final View myview=layoutInflater.inflate(R.layout.prompt,null);
                AlertDialog.Builder alert=new AlertDialog.Builder(List.this);
                alert.setView(myview);
                //alert.setView(myview);
                alert.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText ed=myview.findViewById(R.id.fold);
                        Uri uri=Uri.parse("android.resource://com.test.sitcircular/drawable/plus");
                        mUploadTask=storageRef.child(ed.getText().toString()).child("makeitinvisible.png").putFile(uri);
                        mUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                startActivity(new Intent(List.this,List.class));
                            }
                        });
                    }
                });
                AlertDialog alertDialog=alert.create();
                alertDialog.show();
            }
        });
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.my_menu, m);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            String path = Helper.getPath(this, Uri.parse(data.getData().toString()));
            Uri uri=data.getData();
            Cursor cursor=getContentResolver().query(uri,null,null,null,null);
            int namind=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            switch (requestCode) {
                case 1:
                    uploadFromStream(cursor.getString(namind),data);
                    break;
            }
        }
    }

    private void uploadFromStream(String name, Intent inten) {

//        Helper.showDialog(this);


        storageRef = storageRef.child(name);
        mUploadTask = storageRef.putFile(inten.getData());
        Helper.initProgressDialog(this);
        Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUploadTask.cancel();
            }
        });
        Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Pause", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUploadTask.pause();
            }
        });
        Helper.mProgressDialog.show();
        mUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Helper.dismissProgressDialog();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Helper.dismissProgressDialog();
                startActivity(new Intent(List.this,List.class));
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                Helper.setProgress(progress);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                findViewById(R.id.button_upload_resume).setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.log_out:
                progressDialog.setMessage("Logging Out...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                firebaseAuth.signOut();
                progressDialog.hide();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void showFab() {
        isfabOPen = true;
        folder.animate().translationY(-getResources().getDimension((R.dimen.st55)));
        file.animate().translationY(-getResources().getDimension((R.dimen.st105)));
        add_file.animate().translationY(-getResources().getDimension((R.dimen.st155)));
        floatingActionButton.setImageResource(R.drawable.close);
    }

    private void closeFab() {
        isfabOPen = false;
        folder.animate().translationY(0);
        file.animate().translationY(0);
        add_file.animate().translationY(0);
        floatingActionButton.setImageResource(R.drawable.plus);
    }

    private void refresh()
    {
        arrayList = new ArrayList<>();
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                int flag = 0;
                for (StorageReference item : listResult.getPrefixes()) {
                    arrayList.add(new SubjectData(item.getName(), storageRef, "1"));
                    flag = 1;
                }
                for (StorageReference item : listResult.getItems()) {
                    if(!item.getName().equals("makeitinvisible.png")) {
                        arrayList.add(new SubjectData(item.getName(), storageRef, ""));
                        flag=1;
                    }

                }
                if (flag == 1) {
                    //arrayList.remove(new SubjectData("makeitinvisible.png",storageRef,""));
                    customAdapter = new CustomAdapter(List.this, arrayList, children);
                    listView.setAdapter(customAdapter);
                }
            }
        });
    }

}