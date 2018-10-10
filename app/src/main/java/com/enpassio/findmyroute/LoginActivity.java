package com.enpassio.findmyroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.enpassio.findmyroute.utils.InternetConnectivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.edit_text_email)
    EditText userEmailEditText;

    @BindView(R.id.password_edit_text)
    EditText accountPasswordEditText;

    @BindView(R.id.button_sign_in)
    Button signInButton;

    @BindView(R.id.button_sign_up)
    Button logInButton;

    @BindView(R.id.button_forgot_password)
    Button forgotPassword;

    @BindView(R.id.scroll_view)
    ScrollView scrollView;

    private FirebaseAuth mAuth;

    private String email;
    private String password;
    private String name;
    private String city;
    private String gender;
    private FirebaseDatabase mFirebaseDatabase;

    //Signin button
    private LinearLayout googleSignInButton;

    //Signing Options
    private GoogleSignInOptions gso;

    //google api client
    private GoogleApiClient mGoogleApiClient;

    //Signin constant to check the activity result
    private int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // Hide the Scollbar
        scrollView.setVerticalScrollBarEnabled(false);

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        //Initializing google signin option
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton = (LinearLayout) findViewById(R.id.google_login_container);
        googleSignInButton.setOnClickListener(this);

    }

    @OnClick(R.id.button_sign_in)
    public void signInButtonClicked() {
        email = userEmailEditText.getText().toString().trim();
        password = accountPasswordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            userEmailEditText.setError(getResources().getString(R.string.error_enter_valid_email));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            accountPasswordEditText.setError(getResources().getString(R.string.error_password_less_than_six_character_long));
            return;
        }
        if (InternetConnectivity.isInternetConnected(LoginActivity.this)) {
            SignInWithEmailPassword(email, password);
        } else {
            Toast.makeText(LoginActivity.this, getResources()
                    .getString(R.string.check_internet_connectivity), Toast.LENGTH_SHORT).show();
        }
    }

    private void SignInWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            checkIfEmailVerified();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getResources()
                                            .getString(R.string.toast_authentication_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            //restart this activity
        }
    }

    @OnClick(R.id.button_sign_up)
    public void signUpButtonClicked() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }

    @OnClick(R.id.button_forgot_password)
    public void resetPasswordButtonClicked() {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_login_container:
                signIn();
                break;
        }
    }

    //This function will option signing intent
    private void signIn() {
        //Creating an intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        //Starting intent for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If signin
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
        }
    }

    //After the signing we are calling this function
    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.create_account_to_save_routes), Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        } else {
            //If login fails
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
