package com.example.allam.unkown.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.allam.unkown.R;
import com.example.allam.unkown.model.User;
import com.example.allam.unkown.utiles.Utiles;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private Button mCreateAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private TextView mSighnInTextView;
    private User user;
    private String mUserName, mUserEmail, mPassword;
    private ProgressDialog mAuthProgressDialog;
    private ImageView mLogoImageView;


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initializeScreen();
        mLogoImageView = (ImageView) findViewById(R.id.create_account_logo_image);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        mLogoImageView.startAnimation(animation);
        //Setup Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();

                if (mFirebaseUser != null) {
                    Toast.makeText(CreateAccountActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    //update User display name
                    UserProfileChangeRequest UPCR = new UserProfileChangeRequest.Builder().setDisplayName(mUserName).build();
                    mFirebaseUser.updateProfile(UPCR);
                    sendVerificationEmail();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Error occured, try again!", Toast.LENGTH_SHORT).show();
                }
            }
        };


        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccountinFirebase();
            }
        });


        mSighnInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


    }

    private void initializeScreen() {
        mSighnInTextView = (TextView) findViewById(R.id.tv_sign_in);
        mCreateAccountButton = (Button) findViewById(R.id.btn_create_account_final);
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);

        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }


    private void createAccountinFirebase() {
        if (Utiles.isUserNameValid(getBaseContext(), mEditTextUsernameCreate) &&
                Utiles.isEmailValid(getBaseContext(), mEditTextEmailCreate) &&
                Utiles.isPasswordValid(getBaseContext(), mEditTextPasswordCreate)) {
            mUserName = mEditTextUsernameCreate.getText().toString();
            mUserEmail = mEditTextEmailCreate.getText().toString();
            mPassword = mEditTextPasswordCreate.getText().toString();
        } else {
            return;
        }

        mAuthProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(mUserEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuthProgressDialog.dismiss();
                if (!task.isSuccessful()) {
                    Utiles.setEditTextError(mEditTextEmailCreate, task.getException().getMessage());
                } else {
                    mAuth.addAuthStateListener(mAuthStateListener);
                }
            }
        });

    }

    private void sendVerificationEmail(){
        mAuthProgressDialog.show();
        mAuthProgressDialog.setMessage("Sending email verification...");

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if (task.isSuccessful()) {
                        mAuthProgressDialog.dismiss();
                        Toast.makeText(getBaseContext(),
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                        // start loginActivity after sending verification email
                        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Log.e("AAA", "sendEmailVerification", task.getException());
                        Toast.makeText(getBaseContext(),
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


}
