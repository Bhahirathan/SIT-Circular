package com.test.sitcircular;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    EditText mEmail;
    EditText mPassword;
    private ProgressDialog progressDialog;
    Button mSign_inBtn;
    Button mAdminBtn;
    FirebaseAuth fAuth=FirebaseAuth.getInstance();
    private DatabaseReference databaseReference;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "This app requires File Access Permission", Toast.LENGTH_SHORT).show();
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                        }
                    }, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "File Access Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main);
        getSupportActionBar().hide();
        isStoragePermissionGranted();
        progressDialog=new ProgressDialog(this);
li();
        //Over here

    }
    void li()
    {
        if(getIntent().hasExtra("title"))
        {
            String path=getIntent().getStringExtra("title");
            String file=getIntent().getStringExtra("message");
            if(file.contains("/"))
                file=file.replaceAll("/"+path,"");
            else
                file="/";
            Intent intent=new Intent(getApplicationContext(),List.class);
            //intent.putExtra("b",b);
            intent.putExtra("path",file);
            try {
                //Toast.makeText(getApplicationContext(),String.valueOf(file.lastIndexOf("/")),Toast.LENGTH_LONG).show();
                file = file.substring(0, file.lastIndexOf("/"));
            } catch (Exception e) {
                file="/";
            }
            intent.putExtra("p",file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        if(fAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(getApplicationContext(),List.class));
        }

        databaseReference= FirebaseDatabase.getInstance().getReference("Faculties");
        mEmail = findViewById(R.id.editText);
        mPassword = findViewById(R.id.editText2);
        mSign_inBtn = findViewById( R.id.button );
        mAdminBtn = findViewById( R.id.button3);
        mSign_inBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                ValueEventListener eventListener=new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean flag=false;
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String val = d.getValue(String.class);
                            if (val.equals(email)) {
                                flag=true;
                                if (TextUtils.isEmpty(email)) {
                                    mEmail.setError("Email is Required");
                                    return;
                                }
                                if (TextUtils.isEmpty(password)) {
                                    mPassword.setError("Password is Required");
                                    return;
                                }
                                if (password.length() < 6) {
                                    mPassword.setError("Password Must be >=6 Characters");
                                    return;
                                }
                                progressDialog.setMessage("Logging In...");
                                progressDialog.setCancelable(false);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.hide();
                                            Toast.makeText(MainActivity.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
                                            Intent intent= new Intent( getApplicationContext(), List.class );
                                            startActivity(intent);
                                        } else {
                                            progressDialog.hide();
                                            Toast.makeText(MainActivity.this, "Error !" + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        if(flag==false)
                            Toast.makeText(getApplicationContext(),"Invalid Mail ID",Toast.LENGTH_SHORT).show();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                databaseReference.addListenerForSingleValueEvent(eventListener);
            }
        } );
        mAdminBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( getApplicationContext(), Admin.class ) );
            }
        } );
    }
}
