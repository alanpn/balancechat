package firebase.balancechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.tapadoo.alerter.Alerter;

import java.util.Arrays;

import firebase.balancechat.model.Chat;
import firebase.balancechat.model.Message;
import firebase.balancechat.model.User;
import firebase.balancechat.util.Constants;
import firebase.balancechat.util.StringEncoding;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseListAdapter mChatAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mChatDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    private AccountHeader drawerHeader = null;
    private Drawer drawer = null;
    private ImageView addConversationButton;
    private ListView mChatListView;
    private ValueEventListener mValueEventListener;
    private String currentUserEmail;
    private String mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    createUser(user);
                    onSignedInInitialize(user);
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.AppTheme)
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().setRequireName(true).build(),
                                            new AuthUI.IdpConfig.PhoneBuilder().build(),
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            onCreateDrawer(user);
        }
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


    private void createUser(final FirebaseUser user) {
        final DatabaseReference usersRef = database.getReference(Constants.USER_CHILD);
        final String encodedEmail = StringEncoding.encodeString(user.getEmail());
        final DatabaseReference userRef = usersRef.child(encodedEmail);
        final String uid = user.getUid();
        final String username = user.getDisplayName();
        final String phoneNumber = user.getPhoneNumber();
        final String providerId = user.getProviderId();



/*        if (user.getPhotoUrl() == null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getPhotoUrl() != null) {
                    user.setPhotoUrl(userInfo.getPhotoUrl().toString());
                    break;
                }
            }
        }*/


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

    private void onSignedInInitialize(FirebaseUser user) {
        mUsername = user.getDisplayName();
        mChatDatabaseReference = database.getReference()
                .child(Constants.USER_CHILD
                        + "/" + StringEncoding.encodeString(user.getEmail()) + "/"
                        + Constants.CHAT_CHILD);
        mUserDatabaseReference = database.getReference()
                .child(Constants.USER_CHILD);

        hideShowAddChatButton(user);

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
                                            Glide.with(view.getContext())
                                                    .using(new FirebaseImageLoader())
                                                    .load(storageRef)
                                                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                                                    .into(senderPic);
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

    private void hideShowAddChatButton(FirebaseUser user) {
        addConversationButton = (ImageView) findViewById(R.id.add_conversation);
        final String userLoggedIn = user.getEmail();
        final DatabaseReference friendsCheckRef = database.getReference(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(userLoggedIn));
        friendsCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                String strLong = Long.toString(size);
                if (size > 0) {
                    addConversationButton.setVisibility(View.VISIBLE);
                } else {
                    addConversationButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onCreateDrawer(FirebaseUser user) {
/*        String encodedEmail = StringEncoding.encodeString(user.getEmail());
        DatabaseReference usersRef = database.getReference(Constants.USER_CHILD);
        DatabaseReference userRef = usersRef.child(encodedEmail);
        DatabaseReference imgRef = userRef.child(Constants.PROFILE_PICTURE_PATH);*/


        ProfileDrawerItem profile = new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(R.drawable.balance_launcher);
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withIcon(R.drawable.ic_action_account).withName(R.string.drawer_item_contact).withSelectable(false);
        final SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withIcon(R.drawable.ic_action_settings).withDescription(R.string.settings_description).withName(R.string.drawer_item_settings).withSelectable(false);
        final SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withIcon(R.drawable.ic_menu_share).withName(R.string.drawer_item_invitation).withSelectable(false);
        final SwitchDrawerItem item4 = new SwitchDrawerItem().withIdentifier(4).withIcon(R.drawable.ic_menu_slideshow).withName(R.string.drawer_item_slideshow).withChecked(true).withSelectable(false);
        final SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withIcon(R.drawable.ic_action_signout).withName(R.string.drawer_item_signout).withSelectable(false);

        Toolbar toolbar = findViewById(R.id.toolbar);

        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.side_nav_bar)
                .addProfiles(
                        profile
//                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(R.drawable.ic_action_account_circle).withIdentifier(100001)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3,
                        item4,
                        new DividerDrawerItem(),
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(MainActivity.this, ContactActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                sendInvitation();
                            } else if (drawerItem.getIdentifier() == 4) {

                            } else if (drawerItem.getIdentifier() == 5) {
                                AuthUI.getInstance().signOut(MainActivity.this);
                            }

                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .build();


    }


//     add the values to the bundle
/*    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = drawer.saveInstanceState(outState);
        outState = drawerHeader.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }*/

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, Constants.REQUEST_INVITE);
    }

}
