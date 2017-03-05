package com.example.allam.unkown.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.allam.unkown.R;
import com.example.allam.unkown.ui.activity.HomeActivity;
import com.example.allam.unkown.utiles.Utiles;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by Allam on 2/19/2017.
 */


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFirebaseUser;
    private String mUserEmail;
    private String mUserPassword;
    private EditText mEmailEditText, mPasswordEditText;
    private Button mLoginButtonWithPassword;
    private com.google.android.gms.common.SignInButton mGoogleSignInButton;
    private TextView mSighnUpTextView;
    private ProgressDialog mProgressDialog;
    private static final int GOOGLE_SIGN_IN_INTENT = 101;
    private GoogleSignInAccount mGoogleAccount;
    private ImageView mLogoImageView;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeScreen();
        mLogoImageView = (ImageView) findViewById(R.id.login_logo_image);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        mLogoImageView.startAnimation(animation);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };

        mLoginButtonWithPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseSignInWithEmailAndPassword();
            }
        });

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseSignInwithGoogle();
            }
        });

        mSighnUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });


    }

    private void initializeScreen() {
        mSighnUpTextView = (TextView) findViewById(R.id.tv_sign_up);
        mEmailEditText = (EditText) findViewById(R.id.edit_text_email);
        mPasswordEditText = (EditText) findViewById(R.id.edit_text_password);
        mLoginButtonWithPassword = (Button) findViewById(R.id.login_button_with_password);
        mGoogleSignInButton = (SignInButton) findViewById(R.id.login_with_google);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_signin_user_with_firebase));
        mProgressDialog.setCancelable(false);
    }

    private void firebaseSignInWithEmailAndPassword() {
        if (Utiles.isEmailValid(getBaseContext(), mEmailEditText) && Utiles.isPasswordValid(getBaseContext(), mPasswordEditText)) {
            mUserEmail = mEmailEditText.getText().toString();
            mUserPassword = mPasswordEditText.getText().toString();
        } else {
            return;
        }
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(mUserEmail, mUserPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgressDialog.dismiss();
                if (!task.isSuccessful()) {
                    String error_message = task.getException().getMessage();
                    //print error messeage to th email or password editText
                    if (error_message.equals(getString(R.string.error_email_message))) {
                        Utiles.setEditTextError(mEmailEditText, getString(R.string.print_error_email_message));
                    } else if (error_message.equals(getString(R.string.error_password_message))) {
                        Utiles.setEditTextError(mPasswordEditText, getString(R.string.print_error_password_message));
                    } else {
                        Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_LONG).show();
                        Utiles.setEditTextError(mEmailEditText, "");
                        Utiles.setEditTextError(mPasswordEditText, "");
                    }
                }
            }
        });
    }

    private void firebaseSignInwithGoogle() {
        mProgressDialog.show();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_INTENT);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != GOOGLE_SIGN_IN_INTENT) return;
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            mGoogleAccount = result.getSignInAccount();
            firebaseAuthWithGoogle(mGoogleAccount);
        } else {
            Toast.makeText(this, "Google Sign In Failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgressDialog.dismiss();
                if (!task.isSuccessful()) {
                    Log.i("AAA", task.getException().getMessage());
                    Toast.makeText(getBaseContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
