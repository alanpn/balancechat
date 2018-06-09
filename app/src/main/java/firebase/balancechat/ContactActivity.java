package firebase.balancechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import firebase.balancechat.model.User;
import firebase.balancechat.util.Constants;
import firebase.balancechat.util.LoadImage;
import firebase.balancechat.util.StringEncoding;

@SuppressWarnings("FieldCanBeLocal")
public class ContactActivity extends AppCompatActivity {

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mCurrentUsersFriends;
    private FirebaseAuth mFirebaseAuth;

    private final List<String> mUsersFriends = new ArrayList<>();
    private String mCurrentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initializeScreen();
        showUserList();
    }

    private void showUserList() {
        mFriendListAdapter = new FirebaseListAdapter<User>(this, User.class, R.layout.item_contacts, mUserDatabaseReference) {
            @Override
            protected void populateView(final View view, User user, final int position) {

                final String email = StringEncoding.encodeString(user.getEmail());
                //Check if this user is already your friend
                final DatabaseReference friendRef =
                        mFirebaseDatabase.getReference(Constants.FRIEND_CHILD
                                + "/" + mCurrentUserEmail + "/" + StringEncoding.encodeString(email));

                friendRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (email.equals(mCurrentUserEmail)) { // choose self
                            view.findViewById(R.id.friendRow).setVisibility(View.GONE);
                        } else if (dataSnapshot.getValue() != null) { // is friend
                            view.findViewById(R.id.addFriend).setVisibility(View.GONE);
                            view.findViewById(R.id.removeFriend).setVisibility(View.VISIBLE);
                        } else { // isn't
                            view.findViewById(R.id.addFriend).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (user.getProfilePicLocation() != null && user.getProfilePicLocation().length() > 0) {
                    StorageReference storageRef = FirebaseStorage.getInstance()
                            .getReference().child(user.getProfilePicLocation());
                    LoadImage.loadImages(storageRef, (ImageView) view.findViewById(R.id.photoImageView));
                }

                ((TextView) view.findViewById(R.id.messageTextView)).setText(user.getUsername());
                ((TextView) view.findViewById(R.id.nameTextView)).setText(StringEncoding.decodeString(email));
                (view.findViewById(R.id.addFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Add this user to your friends list, by email
                        addNewFriend(email);
                    }
                });
                (view.findViewById(R.id.removeFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Add this user to your friends list, by email
                        removeFriend(email);
                    }
                });
            }
        };

        mListView.setAdapter(mFriendListAdapter);

        mValueEventListener = mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    finish();
                    return;
                }
                mFriendListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeFriend(String friendEmail) {
        //Get current user logged in by email
        final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        final DatabaseReference friendsRef = mFirebaseDatabase.getReference(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(userLoggedIn));
        friendsRef.child(StringEncoding.encodeString(friendEmail)).removeValue();
    }

    private void addNewFriend(String newFriendEmail) {
        //Get current user logged in by email
        final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        //final String newFriendEncodedEmail = StringEncoding.encodeString(newFriendEmail);
        final DatabaseReference friendsRef = mFirebaseDatabase.getReference(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(userLoggedIn));
        //Add friends to current users friends list
        friendsRef.child(newFriendEmail).setValue(newFriendEmail);
    }

    private void initializeScreen() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUserEmail = StringEncoding.encodeString(mFirebaseAuth.getCurrentUser().getEmail());
        //Eventually this list will filter out users that are already your friend
        mUserDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USER_CHILD);
        mCurrentUsersFriends = mFirebaseDatabase.getReference().child(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(mFirebaseAuth.getCurrentUser().getEmail()));

        mListView = (ListView) findViewById(R.id.friendsListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
