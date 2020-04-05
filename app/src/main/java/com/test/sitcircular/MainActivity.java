package com.test.sitcircular;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;

import android.text.TextUtils;
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


public class MainActivity extends AppCompatActivity {

    EditText mEmail;
    EditText mPassword;
    private ProgressDialog progressDialog;
    Button mSign_inBtn;
    Button mAdminBtn;
    FirebaseAuth fAuth=FirebaseAuth.getInstance();
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        if(fAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(this,List.class));
        }
        progressDialog=new ProgressDialog(this);
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
