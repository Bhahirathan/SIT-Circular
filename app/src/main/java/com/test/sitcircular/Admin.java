package com.test.sitcircular;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class Admin extends AppCompatActivity {



    EditText mEmail;
    EditText mPassword;
    Button mLoginBtn;

    private FirebaseAuth fAuth;
    private ProgressDialog ProgressDialog;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_admin);
        databaseReference=FirebaseDatabase.getInstance().getReference("Admins");
        fAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.editText3);
        mPassword = findViewById(R.id.editText4);
        mLoginBtn =  (Button)findViewById( R.id.button2 );
        ProgressDialog=new ProgressDialog(this);
        mLoginBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Email = mEmail.getText().toString().trim();
                final String Password = mPassword.getText().toString().trim();
                ValueEventListener eventListener=new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean flag=false;
                        for (DataSnapshot d:dataSnapshot.getChildren())
                        {
                            String val= d.getValue().toString();
                            if(val.equals(Email))
                            {
                                if(TextUtils.isEmpty(Email)){
                                    Toast.makeText(getApplicationContext(),"Please Enter the Email",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else if(TextUtils.isEmpty(Password)){
                                    Toast.makeText(getApplicationContext(),"Please Enter the Password",Toast.LENGTH_SHORT).show();
                                    return ;
                                }
                                else if(Password.length()<8){
                                    Toast.makeText(getApplicationContext(),"Password must be minimum of 8 characters",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                ProgressDialog.setMessage("Logging In...");
                                ProgressDialog.setCanceledOnTouchOutside(false);
                                ProgressDialog.show();
                                fAuth.signInWithEmailAndPassword( Email, Password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            ProgressDialog.hide();
                                            Intent intent= new Intent( getApplicationContext(), List.class );
                                            startActivity( intent);
                                        } else {
                                            ProgressDialog.hide();
                                            Toast.makeText(getApplicationContext(), "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                } );
                                flag=true;
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


    }
}

