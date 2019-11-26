package com.example.watechpark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ThrowOnExtraProperties;

public class LoginRegister extends AppCompatActivity {

    private TextView text, text1;
    private ImageView image, image2, image3;
    protected Button button, button1, button3;
    private EditText edit, edit1;
    private CheckBox signedInStatus;

    private FirebaseAuth mLoginAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private SignInButton button2;
    int RC_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;


    private ProgressBar progressBar1;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mLoginAuth = FirebaseAuth.getInstance();


        sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        progressBar1.setVisibility(View.GONE);




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser mUser = mLoginAuth.getCurrentUser();
                if (mUser != null) {
                    Toast.makeText(LoginRegister.this, "Welcome back" + mUser.getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginRegister.this, R.string.please_login, Toast.LENGTH_SHORT).show();
                    loginValidate();

                }
            }
        };

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((v.getId())) {
                    case R.id.buttonReg:
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        break;
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((v.getId())) {
                    case R.id.buttonForgot:

                        startActivity(new Intent(getApplicationContext(), ForgotYourPassword.class));

                        break;

                }
            }

        });

        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                switch ((v.getId())) {
                    case R.id.buttonLogin:
                        loginValidate();
                        saveSharedPref();

                        break;

                }
            }

        });

        loadSharedPref();

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((v.getId())) {
                    case R.id.sign_in_button:
                        signIn();

                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mLoginAuth.getCurrentUser();
        if(currentUser != null){
            loadSharedPref();
            Toast.makeText(getApplicationContext(), "Welcome back " + currentUser.getEmail() + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainMenu.class));
        }
    }

    private void saveSharedPref(){
        if(signedInStatus.isChecked()) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", edit.getText().toString());
            editor.putString("password", edit1.getText().toString());
            editor.putBoolean("checkbox", signedInStatus.isChecked());
            editor.apply();
        }else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", "");
            editor.putString("password", "");
            editor.putBoolean("checkbox", false);
            editor.apply();
        }
    }

    private void loadSharedPref(){
        Boolean checked = sharedPref.getBoolean("checkbox", false);
        String userEmail = sharedPref.getString("email", "");
        String userPassword = sharedPref.getString("password", "");

        edit.setText(userEmail);
        edit1.setText(userPassword);
        signedInStatus.setChecked(checked);

    }

    private void signIn(){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(getApplicationContext(), "Welcome! "  + account.getDisplayName() , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginRegister.this, MainMenu.class));

        } catch (ApiException e) {
            Toast.makeText(LoginRegister.this, "Authentication Failed...", Toast.LENGTH_SHORT).show();
        }

    }





    private void loginValidate() {
        final String email = edit.getText().toString();
        final String password = edit1.getText().toString();

        if (edit.getText().toString().isEmpty()) {
            edit.setError(getString(R.string.user_re1));
            edit.requestFocus();
        } else if (edit1.getText().toString().isEmpty()) {
            edit1.setError(getString(R.string.pass_req1));
            edit1.requestFocus();
        } else {

            progressBar1.setVisibility(View.VISIBLE);
            mLoginAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar1.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Welcome! "  + email , Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginRegister.this, MainMenu.class));
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.TRY_AGAIN, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void findAllViews() {
        image = (ImageView) findViewById(R.id.image2);
        image2 = (ImageView) findViewById(R.id.imageView5);
        image3 = (ImageView) findViewById(R.id.imageView6);
        button = (Button) findViewById(R.id.buttonLogin);
        button1 = (Button) findViewById(R.id.buttonReg);
        button2 = (SignInButton) findViewById(R.id.sign_in_button);
        button3 = (Button) findViewById(R.id.buttonForgot);
        edit = (EditText) findViewById(R.id.editUser);
        edit1 = (EditText) findViewById(R.id.editPass);
        text = findViewById(R.id.textUser);
        text1 = findViewById(R.id.textPass);
        progressBar1 = (ProgressBar)findViewById(R.id.progressBar3);
        signedInStatus = findViewById(R.id.checkBox);
        //text2 = findViewById(R.id.textForgot);
    }

}
