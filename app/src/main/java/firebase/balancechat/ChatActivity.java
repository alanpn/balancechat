package firebase.balancechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import firebase.balancechat.model.Chat;
import firebase.balancechat.model.Friend;
import firebase.balancechat.model.Message;
import firebase.balancechat.model.User;
import firebase.balancechat.util.Constants;
import firebase.balancechat.util.LoadImage;
import firebase.balancechat.util.StringEncoding;
import tgio.rncryptor.RNCryptorNative;

@SuppressWarnings("FieldCanBeLocal")
public class ChatActivity extends AppCompatActivity {
    private String TAG = "New Conversation";

    private ListView mListView;
    private Toolbar mToolBar;

    private FirebaseListAdapter mFriendListAdapter;
    private ValueEventListener mValueEventListener;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFriendsLocationDatabaseReference;
    private DatabaseReference mCurrentUserDatabaseReference;
    private DatabaseReference mFriendDatabaseReference;
    private TextView mFriendsInChat;
    private EditText mChatName;


    private Chat mChat;
    private DatabaseReference mUserDatabaseRef;
    private ImageButton mCreateButton;
    private RNCryptorNative RNCryptor = new RNCryptorNative();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        initializeScreen();
        showFriendsList();
        addListeners();
    }

    private void addListeners() {
        mChatName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mCreateButton.setEnabled(true);
                } else {
                    mCreateButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void showFriendsList() {
        mFriendListAdapter = new FirebaseListAdapter<String>(this, String.class, R.layout.item_contacts, mFriendsLocationDatabaseReference) {
            @Override
            protected void populateView(final View view, final String friend, final int position) {
                final Friend addFriend = new Friend(friend);
                ((TextView) view.findViewById(R.id.nameTextView)).setText(StringEncoding.decodeString(friend));

                mUserDatabaseRef.child(friend).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User fUser = dataSnapshot.getValue(User.class);
                        if (fUser != null) {
                            ((TextView) view.findViewById(R.id.messageTextView))
                                    .setText(StringEncoding.decodeString(fUser.getUsername()));
                            if (fUser.getProfilePicLocation() != null && fUser.getProfilePicLocation().length() > 0) {
                                try {
                                    StorageReference storageRef = FirebaseStorage.getInstance()
                                            .getReference().child(fUser.getProfilePicLocation());
                                    LoadImage.loadImages(storageRef, (ImageView) view.findViewById(R.id.photoImageView));
                                } catch (Exception e) {
                                    Log.e("Err", e.toString());
                                }
                            }
                        } else {
                            ((TextView) view.findViewById(R.id.messageTextView))
                                    .setText(R.string.noname);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                if (mChat.getFriends().isEmpty()) {
                    view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                }
                (view.findViewById(R.id.addFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "Clicking row: " + position);
                        Log.e(TAG, "Clicking user: " + friend);


                        if (mChat.appendFriend(addFriend)) {
                            String friendsString = "";
                            for (Friend f : mChat.getFriends()) {
                                friendsString += StringEncoding.decodeString(f.getEmail()) + ", ";
                            }
                            friendsString = friendsString.substring(0, friendsString.length() - 2);
                            mFriendsInChat.setText("Users added to chat: " + friendsString);
                        }
                        view.findViewById(R.id.removeFriend).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.addFriend).setVisibility(View.GONE);
                    }
                });
                (view.findViewById(R.id.removeFriend)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mChat.removeFriend(addFriend);
                        String friendsString = "";
                        for (Friend f : mChat.getFriends()) {
                            friendsString += f.getEmail() + ", ";
                        }
                        if (friendsString.length() > 1) {
                            friendsString = friendsString.substring(0, friendsString.length() - 2);

                            mFriendsInChat.setText("Users added to chat: " + StringEncoding.decodeString(friendsString));
                        } else {
                            mFriendsInChat.setText("Users added to chat: ");
                        }
                        view.findViewById(R.id.addFriend).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.removeFriend).setVisibility(View.GONE);
                    }
                });
            }
        };
        mListView.setAdapter(mFriendListAdapter);

        mValueEventListener = mFriendsLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
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

    //TODO: Add create new Chat function
    public void createChat(View view) {
        // final String userLoggedIn = mFirebaseAuth.getCurrentUser().getEmail();
        // final String newFriendEncodedEmail = EmailEncoding.commaEncodePeriod(newFriendEmail);
        final DatabaseReference chatRef = mFirebaseDatabase.getReference(Constants.CHAT_CHILD);
        final DatabaseReference messageRef = mFirebaseDatabase.getReference(Constants.MESSAGE_CHILD);
        final DatabaseReference pushRef = chatRef.push();
        final String pushKey = pushRef.getKey();
        mChat.setUid(pushKey);
        mChat.setChatName(mChatName.getText().toString());


        HashMap<String, Object> chatItemMap = new HashMap<String, Object>();
        HashMap<String, Object> chatObj = (HashMap<String, Object>) new ObjectMapper()
                .convertValue(mChat, Map.class);
        chatItemMap.put("/" + pushKey, chatObj);
        chatRef.updateChildren(chatItemMap);


        String initialMessage = mFriendsInChat.getText().toString();
        Message initialMessages =
                new Message("System", initialMessage);
        final DatabaseReference initMsgRef =
                mFirebaseDatabase.getReference(Constants.MESSAGE_CHILD + "/" + pushKey);
        final DatabaseReference msgPush = initMsgRef.push();
        final String msgPushKey = msgPush.getKey();
        if (msgPushKey != null) {
            initMsgRef.child(msgPushKey).setValue(initialMessages);
        }

        chatItemMap = new HashMap<String, Object>();
        chatItemMap.put("/chats/" + pushKey, chatObj);
        mCurrentUserDatabaseReference.updateChildren(chatItemMap);


        for (Friend f : mChat.getFriends()) {
            mFriendDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USER_CHILD
                    + "/" + StringEncoding.encodeString(f.getEmail()));
            chatItemMap = new HashMap<String, Object>();
            chatItemMap.put("/chats/" + pushKey, chatObj);
            mFriendDatabaseReference.updateChildren(chatItemMap);
            mFriendDatabaseReference = null;
        }

        Intent intent = new Intent(view.getContext(), ChatMessagesActivity.class);
        String messageKey = pushKey;
        intent.putExtra(Constants.MESSAGE_ID, messageKey);
        intent.putExtra(Constants.CHAT_NAME, mChat.getChatName());
        startActivity(intent);
    }

    private void initializeScreen() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserDatabaseRef = mFirebaseDatabase.getReference().child(Constants.USER_CHILD);
        mCurrentUserDatabaseReference = mFirebaseDatabase.getReference().child(Constants.USER_CHILD
                + "/" + StringEncoding.encodeString(mFirebaseAuth.getCurrentUser().getEmail()));
        //Eventually this list will filter out users that are already your friend
        mFriendsLocationDatabaseReference = mFirebaseDatabase.getReference().child(Constants.FRIEND_CHILD
                + "/" + StringEncoding.encodeString(mFirebaseAuth.getCurrentUser().getEmail()));

        mListView = (ListView) findViewById(R.id.conversationListView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle("Create new chat");

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCreateButton = (ImageButton) findViewById(R.id.createButton);

        mFriendsInChat = (TextView) findViewById(R.id.friendsInChat);
        mChatName = (EditText) findViewById(R.id.chat_name);
        mChat = new Chat("", "");
    }
}
