package firebase.balancechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tapadoo.alerter.Alerter;

import java.util.Arrays;

import firebase.balancechat.model.Chat;
import firebase.balancechat.model.Message;
import firebase.balancechat.model.User;
import firebase.balancechat.util.Constants;
import firebase.balancechat.util.LoadImage;
import firebase.balancechat.util.StringEncoding;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseListAdapter mChatAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mChatDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ImageView addConversationButton;
    private ListView mChatListView;
    private ValueEventListener mValueEventListener;
    private String currentUserEmail;
    private String mUsername;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onCreateUser(user);
                    onSignIn(user);
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setLogo(R.drawable.balance_launcher)
                                    .setPrivacyPolicyUrl("https://policies.google.com/privacy")
                                    .setTheme(R.style.AppTheme)
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().setRequireName(true).build(),
//                                            new AuthUI.IdpConfig.PhoneBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            Constants.SIGN_IN_REQUEST_CODE);
                }
            }
        };
    }


    public void createNewChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    /* connection test at the entry */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (requestCode == Constants.SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Alerter.create(this)
                        .setTitle(getString(R.string.greeting) + " " + user.getDisplayName())
                        .setBackgroundColorRes(R.color.colorDeepTeal)
                        .setIcon(R.drawable.ic_action_info)
                        .show();
            } else {
                Alerter.create(this)
                        .setTitle(R.string.login_fail)
                        .show();
                finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            onCreateDrawer(user);
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.action_menu);
        floatingActionMenu.close(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*

     *//* create channel to show notifications (API 27+) *//*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }


        *//* token output *//*
        Button logTokenButton = findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

     */


    private void onCreateUser(final FirebaseUser user) {
        final DatabaseReference usersRef = database.getReference(Constants.USER_CHILD);
        final String encodedEmail = StringEncoding.encodeString(user.getEmail());
        final DatabaseReference userRef = usersRef.child(encodedEmail);
        final String uid = user.getUid();
        final String username = user.getDisplayName();
        final String phoneNumber = user.getPhoneNumber();
        final String providerId = user.getProviderId();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    User newUser = new User(username, encodedEmail, uid, phoneNumber, providerId);
                    userRef.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onSignIn(@NonNull FirebaseUser user) {
        mUsername = user.getDisplayName();
        mChatDatabaseReference = database.getReference()
                .child(Constants.USER_CHILD
                        + "/" + StringEncoding.encodeString(user.getEmail()) + "/"
                        + Constants.CHAT_CHILD);
        mUserDatabaseReference = database.getReference()
                .child(Constants.USER_CHILD);

        conversationButtonTest(user);

        String imageLocation = Constants.PROFILE_PICTURE_PATH + currentUserEmail;
        ImageView drawerImage = (ImageView) findViewById(R.id.accountImageView);
        TextView drawerUsername = (TextView) findViewById(R.id.accountNameView);
        TextView drawerEmail = (TextView) findViewById(R.id.accountEmailView);
        drawerUsername.setText(user.getDisplayName());
        drawerEmail.setText(user.getEmail());
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference().child(imageLocation);
        LoadImage.loadImages(storageRef, drawerImage);

        //Initialize screen variables
        mChatListView = (ListView) findViewById(R.id.chatListView);

        mChatAdapter = new FirebaseListAdapter<Chat>(this, Chat.class, R.layout.item_chats, mChatDatabaseReference) {
            @Override
            protected void populateView(final View view, Chat chat, final int position) {
                //final Friend addFriend = new Friend(chat);
                ((TextView) view.findViewById(R.id.messageTextView)).setText(chat.getChatName());

                //Fetch last message from chat
                final DatabaseReference messageRef =
                        database.getReference(Constants.MESSAGE_CHILD
                                + "/" + chat.getUid());

                final TextView latestMessage = (TextView) view.findViewById(R.id.nameTextView);
                final ImageView senderPic = (ImageView) view.findViewById(R.id.photoImageView);
                final ImageView drawerAccountView = (ImageView) findViewById(R.id.accountImageView);

                messageRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                        Message newMsg = dataSnapshot.getValue(Message.class);
                        latestMessage.setText(StringEncoding.decodeString(newMsg.getSender()) + ": " + newMsg.getMessage());

                        mUserDatabaseReference.child(newMsg.getSender())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User msgSender = dataSnapshot.getValue(User.class);
                                        if (msgSender != null && msgSender.getProfilePicLocation() != null) {
                                            StorageReference storageRef = FirebaseStorage.getInstance()
                                                    .getReference().child(msgSender.getProfilePicLocation());
                                            LoadImage.loadImages(storageRef, senderPic);

                                            LoadImage.loadImages(storageRef, drawerAccountView);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                //Replace this with the most recent message from the chat

            }
        };

        mChatListView.setAdapter(mChatAdapter);
        mChatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String messageLocation = mChatAdapter.getRef(position).toString();

                if (messageLocation != null) {
                    Intent intent = new Intent(view.getContext(), ChatMessagesActivity.class);
                    String messageKey = mChatAdapter.getRef(position).getKey();
                    intent.putExtra(Constants.MESSAGE_ID, messageKey);
                    Chat chatItem = (Chat) mChatAdapter.getItem(position);
                    intent.putExtra(Constants.CHAT_NAME, chatItem.getChatName());
                    startActivity(intent);
                }
            }
        });

        mValueEventListener = mChatDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                //Check if any chats exists
                if (chat == null) {
                    //finish();
                    return;
                }
                mChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void conversationButtonTest(FirebaseUser user) {
        addConversationButton = (ImageView) findViewById(R.id.add_conversation);
        final String userLoggedIn = user.getEmail();
        final DatabaseReference friendsCheckRef = database.getReference(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(userLoggedIn));
        friendsCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                if (size > 0) {
                    addConversationButton.setClickable(true);
                } else {
                    addConversationButton.setClickable(false);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_contacts) {
            intent = new Intent(MainActivity.this, ContactActivity.class);
        } else if (id == R.id.nav_share) {
            sendInvitation();
        } else if (id == R.id.nav_settings) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);
        } else if (id == R.id.nav_signout) {
            AuthUI.getInstance().signOut(this);
        }

        if (intent != null) {
            MainActivity.this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, Constants.REQUEST_INVITE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
