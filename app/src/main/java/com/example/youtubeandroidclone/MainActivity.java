package com.example.youtubeandroidclone;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.youtubeandroidclone.Fragments.ExploreFragment;
import com.example.youtubeandroidclone.Fragments.HomeFragment;
import com.example.youtubeandroidclone.Fragments.LibraryFragment;
import com.example.youtubeandroidclone.Fragments.SubscriptionsFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;

    AppBarLayout appBarLayout;
    Fragment fragment;

    ImageView user_profile_image;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private static final int PERMISSION = 101;
    private static final int PICK_VIDEO = 102;

    Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameLayout = findViewById(R.id.frame_layout);
        appBarLayout = findViewById(R.id.appBar);

        user_profile_image = findViewById(R.id.user_profile_image);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        checkPermission();

        if(firebaseUser != null) {
            getProfileImage();
        }

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        user_profile_image.setOnClickListener(v -> {
            if(firebaseUser != null) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                getProfileImage();
            } else {
                user_profile_image.setImageResource(R.drawable.account);
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

        showFragment();

        HomeFragment homeFragment = new HomeFragment();
        selectedFragment(homeFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    selectedFragment(homeFragment);
                    break;
                case R.id.explore:
                    ExploreFragment exploreFragment = new ExploreFragment();
                    selectedFragment(exploreFragment);
                    break;
                case R.id.publish:
                    showPublishContentDialog();
                    break;
                case R.id.library:
                    LibraryFragment libraryFragment = new LibraryFragment();
                    selectedFragment(libraryFragment);
                    break;
                case R.id.subscriptions:
                    SubscriptionsFragment subscriptionsFragment = new SubscriptionsFragment();
                    selectedFragment(subscriptionsFragment);
                    break;
            }
            return false;
        });
    }

    private void showPublishContentDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upload_dialog);
        dialog.setCanceledOnTouchOutside(true);

        TextView text_upload_video = dialog.findViewById(R.id.text_upload_video);
        TextView text_make_post = dialog.findViewById(R.id.text_publish_post);
        TextView text_poll = dialog.findViewById(R.id.text_release_poll);

        text_upload_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
            }
        });
        dialog.show();
    }

    private void selectedFragment(Fragment fragment) {
        setStatusBarColor("#FFFFFF");
        appBarLayout.setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification:
                Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if(resultCode == RESULT_OK && data != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task1 -> {
                            if(task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                HashMap<String, Object> newUser = new HashMap<>();
                                newUser.put("username", account.getDisplayName());
                                newUser.put("email", account.getEmail());
                                newUser.put("profile", String.valueOf(account.getPhotoUrl()));
                                newUser.put("uid", firebaseUser.getUid());
                                newUser.put("search", account.getDisplayName().toLowerCase());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                                reference.child(firebaseUser.getUid()).setValue(newUser);
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            case PICK_VIDEO:
                if(resultCode == RESULT_OK && data != null) {
                    videoUri = data.getData();
                    Intent intent = new Intent(MainActivity.this, PublishContentActivity.class);
                    intent.putExtra("type", "video");
                    intent.setData(videoUri);
                    startActivity(intent);
                }
        }
    }

    private void getProfileImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String profile = snapshot.child("profile").getValue().toString();
                    Picasso.get().load(profile).placeholder(R.drawable.account)
                            .into(user_profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFragment() {
        String type = getIntent().getStringExtra("type");
        if(type != null) {
            switch (type) {
                case "channel":
                    setStatusBarColor("#99FF0000");
                    appBarLayout.setVisibility(View.GONE);
                    fragment = ChannelDashboardFragment.newInstance();
                    break;
            }
            if(fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setStatusBarColor(String color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(color));
    }

    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION);
        } else {
            Log.d("tag", "checkPermission: Permission Granted");
        }
    }
}
