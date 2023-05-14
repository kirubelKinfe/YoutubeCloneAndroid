package com.example.youtubeandroidclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView user_profile_image;

    TextView text_username, text_email, text_your_channel, text_settings, text_help;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;

    String userName,email,profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        init();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        getData();

        text_your_channel.setOnClickListener(v -> {
            reference.child("Channels").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                        intent.putExtra("type", "channel");
                        startActivity(intent);
                    } else {
                        showChannelDialog();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void init() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        user_profile_image = findViewById(R.id.user_profile_image);
        text_username = findViewById(R.id.user_channel_name);
        text_email = findViewById(R.id.user_email);
        text_your_channel = findViewById(R.id.text_channel_name);
        text_settings = findViewById(R.id.text_settings);
        text_help = findViewById(R.id.text_help);
    }

    private void getData() {
        reference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    userName = snapshot.child("username").getValue().toString();
                    email = snapshot.child("email").getValue().toString();
                    profile = snapshot.child("profile").getValue().toString();

                    text_username.setText(userName);
                    text_email.setText(email);

                    try {
                        Picasso.get().load(profile).placeholder(R.drawable.account).into(user_profile_image);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChannelDialog() {
        Dialog dialog = new Dialog(AccountActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.channel_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText input_channel_name = dialog.findViewById(R.id.input_channel_name);
        EditText input_description = dialog.findViewById(R.id.input_description);
        TextView text_create_channel = dialog.findViewById(R.id.text_create_channel);

        text_create_channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = input_channel_name.getText().toString().trim();
                String description = input_description.getText().toString().trim();

                if(name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(AccountActivity.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    createNewChannel(name, description, dialog);
                }
            }
        });
        dialog.show();
    }

    private void createNewChannel(String name, String description, Dialog dialog) {
        ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
        progressDialog.setTitle("New Channel");
        progressDialog.setMessage("Creating...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String date = DateFormat.getDateInstance().format(new Date());

        HashMap<String, Object> map = new HashMap<>();
        map.put("channel_name", name);
        map.put("description", description);
        map.put("joined", date);
        map.put("uid", user.getUid());
        map.put("channel_logo", profile);

        reference.child("Channels").child(user.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    progressDialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(AccountActivity.this, name + " channel has been created", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(AccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}