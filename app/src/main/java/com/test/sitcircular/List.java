package com.test.sitcircular;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.FragmentActivity;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.sitcircular.util.Helper;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class List extends AppCompatActivity{
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    AlertDialog.Builder alert;
    private CustomAdapter customAdapter;
    private ArrayList<SubjectData> arrayList;
    private FloatingActionButton floatingActionButton, folder, file,add_file;
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;
    private boolean isfabOPen = false;
    private UploadTask mUploadTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private Bundle b;
    int f;
    private StorageReference storageRef,stref;
    private ProgressBar p;
    private BiometricPrompt myBiometricPrompt;
    private  BiometricPrompt.PromptInfo promptInfo;
    private FingerprintManagerCompat fingerprintManagerCompat;

    @SuppressLint({"RestrictedApi", "ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_list);
        setProgressBarIndeterminateVisibility(true);
        progressDialog = new ProgressDialog(this);
        alert=new AlertDialog.Builder(this);
        f=0;
        p=findViewById(R.id.pbar);
        floatingActionButton = findViewById(R.id.fab);
        folder = findViewById(R.id.folder);
        file = findViewById(R.id.user);
        add_file = findViewById(R.id.add_file);
        swipeRefreshLayout=findViewById(R.id.swipe);
        floatingActionButton.setVisibility(View.GONE);
        add_file.setVisibility(View.GONE);
        file.setVisibility(View.GONE);
        folder.setVisibility(View.GONE);

        Executor newExecutor = Executors.newSingleThreadExecutor();
        FragmentActivity activity = this;
        myBiometricPrompt = new BiometricPrompt(activity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    moveTaskToBack(true);
                } else {
                    moveTaskToBack(true);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //over here
                li();
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                myBiometricPrompt.authenticate(promptInfo);
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("App Locked")
                .setSubtitle("Touch the fingerprint sensor")
                .setNegativeButtonText("Cancel").setConfirmationRequired(true)
                .build();
        Intent i = getIntent();
        b = i.getExtras();
        fingerprintManagerCompat=FingerprintManagerCompat.from(getApplicationContext());
        storage = FirebaseStorage.getInstance();
        stref=storageRef = storage.getReference();
        if (b != null) {
            if(!Objects.requireNonNull(b.getString("p")).equals(""))
                stref=storageRef.child(Objects.requireNonNull(b.getString("p")));
            storageRef=storageRef.child(Objects.requireNonNull(b.getString("path")));
            //Toast.makeText(getApplicationContext(),storageRef.getPath(),Toast.LENGTH_LONG).show();
            li();
        }
        else
        {
            if(f==0) {
                f=1;
                if(fingerprintManagerCompat.hasEnrolledFingerprints()&&fingerprintManagerCompat.isHardwareDetected())
                myBiometricPrompt.authenticate(promptInfo);
                else
                    li();
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic("notifications");
    }

    @SuppressLint("ResourceAsColor")
    void li()
    {
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Admins");
        ValueEventListener eventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String val = d.getValue(String.class);
                    if (Objects.requireNonNull(val).equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
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

        TextView textView=findViewById(R.id.path);
        textView.setText("Home "+storageRef.getPath().replaceAll("/"," > "));
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater=LayoutInflater.from(List.this);
                final View myview=layoutInflater.inflate(R.layout.create_user,null);
                AlertDialog.Builder alert=new AlertDialog.Builder(List.this);
                alert.setView(myview);
                alert.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        RadioGroup Rg=myview.findViewById(R.id.type);
                        RadioButton rb=myview.findViewById(Rg.getCheckedRadioButtonId());
                        final EditText et=myview.findViewById(R.id.mail);
                        if(rb.getText().equals("Admin"))
                        {
                            firebaseAuth.createUserWithEmailAndPassword(et.getText().toString(),"afdakhftqgvfd").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    firebaseAuth.sendPasswordResetEmail(et.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            databaseReference.push().setValue(et.getText().toString());
                                            progressDialog.hide();
                                            Toast.makeText(getApplicationContext(),et.getText()+ " added Successfully as an Admin",Toast.LENGTH_SHORT).show();
                                            firebaseAuth.signOut();
                                            Toast.makeText(getApplicationContext(),"Please Sign in again for verification",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),List.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                        else if(rb.getText().equals("Faculty"))
                        {
                            firebaseAuth.createUserWithEmailAndPassword(et.getText().toString(),"afdakhftqgvfd").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    firebaseAuth.sendPasswordResetEmail(et.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FirebaseDatabase.getInstance().getReference().child("Faculties").push().setValue(et.getText().toString());
                                            progressDialog.hide();
                                            Toast.makeText(getApplicationContext(),et.getText()+ " added Successfully as a Faculty",Toast.LENGTH_SHORT).show();
                                            firebaseAuth.signOut();
                                            Toast.makeText(getApplicationContext(),"Please Sign in again for verification",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),Admin.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                AlertDialog alertDialog=alert.create();
                alertDialog.show();
            }
        });
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
                @SuppressLint("InflateParams") final View myview=layoutInflater.inflate(R.layout.prompt,null);
                AlertDialog.Builder alert=new AlertDialog.Builder(List.this);
                alert.setView(myview);
                alert.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Creating Folder...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        EditText ed=myview.findViewById(R.id.fold);
                        Uri uri=Uri.parse("android.resource://com.test.sitcircular/drawable/plus");
                        mUploadTask=storageRef.child(ed.getText().toString()).child("makeitinvisible.png").putFile(uri);
                        mUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                refresh();
                                progressDialog.hide();
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
        if(f!=0)
            if(fingerprintManagerCompat.hasEnrolledFingerprints()&&fingerprintManagerCompat.isHardwareDetected())
                myBiometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            Uri uri=data.getData();
            @SuppressLint("Recycle") Cursor cursor=getContentResolver().query(Objects.requireNonNull(uri),null,null,null,null);
            int namind= Objects.requireNonNull(cursor).getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            switch (requestCode) {
                case 1:
                    uploadFromStream(cursor.getString(namind),data);
                    break;
            }
        }
    }
    @Override
    public void onBackPressed()
    {
        if(storageRef.getPath().equals("/")) {
            alert.setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no,null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
            AlertDialog dialog=alert.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.TRANSPARENT);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#004b8e"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#004b8e"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), List.class);
            Bundle b=new Bundle();
            intent.putExtra("b",b);
            intent.putExtra("path",stref.getPath());
            if(stref.getParent()!=null)
            intent.putExtra("p",stref.getParent().getPath());
            else
                intent.putExtra("p","/");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void uploadFromStream(String name, Intent inten) {
        mUploadTask = storageRef.child(name).putFile(Objects.requireNonNull(inten.getData()));
        Helper.initProgressDialog(this);
        Helper.mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUploadTask.cancel();
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
                refresh();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                Helper.setProgress(progress);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

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
                FirebaseMessaging.getInstance().unsubscribeFromTopic("notifications");
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

    public void refresh() {

            arrayList = new ArrayList<>();
            storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    int flag = 0;
                    for (StorageReference item : listResult.getPrefixes()) {
                        //Toast.makeText(getApplicationContext(),"R "+storageRef.getName(),Toast.LENGTH_SHORT).show();
                        arrayList.add(new SubjectData(item.getName(), storageRef, "1"));
                        flag = 1;
                    }
                    for (StorageReference item : listResult.getItems()) {
                        if (!item.getName().equals("makeitinvisible.png")) {
                            arrayList.add(new SubjectData(item.getName(), storageRef, ""));
                            flag = 1;
                        }
                    }
                    if (flag == 1) {
                        customAdapter = new CustomAdapter(List.this, arrayList);
                        listView.setAdapter(customAdapter);
                    }
                    p.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.my_menu, m);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView search = (SearchView) m.findItem(R.id.search).getActionView();
            search.setQueryHint("Enter the keyword...");
            search.setSearchableInfo(Objects.requireNonNull(manager).getSearchableInfo(getComponentName()));
            final Handler h=new Handler();
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(final String query) {
                    h.removeCallbacksAndMessages(null);
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(query.equals(""))
                                refresh();
                            else
                            {
                                p.setVisibility(View.VISIBLE);
                                arrayList = new ArrayList<>();
                                search(query.toLowerCase(),storageRef);
                            }

                        }
                    },500);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return true;
                }
            });

        }
        return true;
    }
    void search(final String key, final StorageReference sr)
    {
        sr.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getPrefixes()) {
                    if (item.getName().toLowerCase().startsWith(key)) {
                        arrayList.add(new SubjectData(item.getName(), sr, "1"));
                    }
                    search(key, item);
                }
                for (StorageReference item : listResult.getItems()) {
                    if (!item.getName().equals("makeitinvisible.png") && item.getName().toLowerCase().startsWith(key)) {
                        arrayList.add(new SubjectData(item.getName(), sr, ""));
                    }
                }
                if (arrayList.size() > 0)
                    customAdapter = new CustomAdapter(List.this, arrayList);
                listView.setAdapter(customAdapter);
                p.setVisibility(View.GONE);
            }
        });
    }
public void onPause()
{
    super.onPause();
}
}
