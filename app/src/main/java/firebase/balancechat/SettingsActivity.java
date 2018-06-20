package firebase.balancechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tapadoo.alerter.Alerter;

import java.util.UUID;

import firebase.balancechat.model.User;
import firebase.balancechat.util.Constants;
import firebase.balancechat.util.LoadImage;
import firebase.balancechat.util.StringEncoding;

@SuppressWarnings("FieldCanBeLocal")
public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private Toolbar toolbar;
    private ImageButton photoPickerButton;
    private TextInputLayout textItem;
    private EditText editText;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private ImageView profileImage;
    private DatabaseReference databaseReference;
    private Context mView;
    private String currentUserEmail;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mView = SettingsActivity.this;
        initializeScreen();
        openImageSelector();
        editUsername();
        editPassword();
        initializeUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        storageReference = FirebaseStorage.getInstance().getReference();
        super.onActivityResult(requestCode, requestCode, data);

        if (requestCode == Constants.GALLERY_INTENT && resultCode == RESULT_OK) {

            progressDialog.setMessage(getString(R.string.waiting_for_loading));
            progressDialog.show();

            Uri uri = data.getData();
            final String imageLocation = Constants.PROFILE_PICTURE_PATH + currentUserEmail;
            final String imageLocationId = imageLocation + "/" + uri.getLastPathSegment();
            final String uniqueId = UUID.randomUUID().toString();
//            final StorageReference filepath = storageReference.child(imageLocation).child(uniqueId + Constants.PROFILE_PICTURE_PATH);
            final StorageReference filepath = storageReference.child(imageLocation).child(uniqueId);
            final String downloadURl = filepath.getPath();
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    addImageToProfile(downloadURl);
                    progressDialog.dismiss();
                }
            });
        }

    }

    public void addImageToProfile(final String imageLocation) {
        final ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
        databaseReference
                .child(Constants.PROFILE_PICTURE_LOCATION_CHILD).setValue(imageLocation).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        StorageReference storageRef = FirebaseStorage.getInstance()
                                .getReference().child(imageLocation);
                        LoadImage.loadImages(storageRef, imageView);

                    }
                }
        );

    }

    public void openImageSelector() {
        photoPickerButton = (ImageButton) findViewById(R.id.imageButton);
        progressDialog = new ProgressDialog(this);
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, Constants.GALLERY_INTENT);
            }
        });
    }

    public void editPassword() {
        photoPickerButton = (ImageButton) findViewById(R.id.imageButton1);
        editText = (EditText) findViewById(R.id.settingsInputEditText1);
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringPassword = editText.getText().toString();
                if (stringPassword.equals("")) {
                    Alerter.create(SettingsActivity.this)
                            .setTitle("Enter new password")
                            .setBackgroundColorRes(R.color.colorWarning)
                            .setIcon(R.drawable.ic_action_warning)
                            .show();
                    return;
                }
                FirebaseAuth.getInstance().getCurrentUser().updatePassword(stringPassword);
                Alerter.create(SettingsActivity.this)
                        .setTitle("Your password updated")
                        .setBackgroundColorRes(R.color.colorDeepTeal)
                        .setIcon(R.drawable.ic_action_info)
                        .show();

            }
        });
    }

    public void editUsername() {
        photoPickerButton = (ImageButton) findViewById(R.id.imageButton2);
        editText = (EditText) findViewById(R.id.settingsInputEditText2);
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringEmail = editText.getText().toString();
                if (stringEmail.equals("")) {
                    Alerter.create(SettingsActivity.this)
                            .setTitle("Enter new username")
                            .setBackgroundColorRes(R.color.colorWarning)
                            .setIcon(R.drawable.ic_action_warning)
                            .show();
                    return;
                }
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(stringEmail)
                        .build();
                FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);
                Alerter.create(SettingsActivity.this)
                        .setTitle("Your username updated")
                        .setBackgroundColorRes(R.color.colorDeepTeal)
                        .setIcon(R.drawable.ic_action_info)
                        .show();

            }
        });
    }


    public void initializeUserInfo() {
        final ImageView imageView = (ImageView) findViewById(R.id.profilePicture);
        databaseReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null && user.getProfilePicLocation() != null) {
                            StorageReference storageRef = FirebaseStorage.getInstance()
                                    .getReference().child(user.getProfilePicLocation());


                            LoadImage.loadImages(storageRef, imageView);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initializeScreen() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUserEmail = StringEncoding.encodeString(firebaseAuth.getCurrentUser().getEmail());
        databaseReference = firebaseDatabase
                .getReference().child(Constants.USER_CHILD
                        + "/" + currentUserEmail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
