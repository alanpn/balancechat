package firebase.balancechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.irozon.sneaker.Sneaker;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int SIGN_IN_REQUEST_CODE = 123;
    private static final int MAX_MSG_LENGTH = 150;
    private static final int REQUEST_INVITE = 1;
    public static final String MESSAGES_CHILD = "messages";
//    private static final String TAG = "MainActivity";

    private RelativeLayout activity_main;
    private FloatingActionButton sendMsgButton;
    private FirebaseListAdapter<ChatMessage> adapter;

    private FirebaseUser user = null;

    private AccountHeader drawerHeader = null;
    private Drawer drawer = null;


    /* menu items select */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // TODO: on tap menu_settings.
            Snackbar.make(activity_main, "No action.", Snackbar.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.message_delete) {
            // TODO: on tap message_delete.
            Snackbar.make(activity_main, "No action.", Snackbar.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, R.string.sign_out_msg, Snackbar.LENGTH_SHORT).show();
//                    finish();
                    requestSignIn();
                    displayChat();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    /* connection test at the entry */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, getString(R.string.greeting) + user.getDisplayName(), Snackbar.LENGTH_SHORT).show();
                displayChat();
            } else {
                Snackbar.make(activity_main, R.string.login_fail, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        setContentView(R.layout.activity_main);
        activity_main = findViewById(R.id.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        authTest();

        sendMsgButton = findViewById(R.id.sendMsgButton);
        sendMsgButton.setOnClickListener(new View.OnClickListener() {

            /* push/input into DB */
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.input);
                String msg = input.getText().toString();

                if (msg.equals("")) {
                    Sneaker.with(MainActivity.this)
                            .setTitle(getString(R.string.EMPTY_MSG))
                            .sneakWarning();
                    return;
                }

                if (msg.length() > MAX_MSG_LENGTH) {
                    Sneaker.with(MainActivity.this)
                            .setTitle(getString(R.string.MSG_IS_TOO_LONG) + MAX_MSG_LENGTH)
                            .sneakWarning();
                    return;
                }

                    FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD).push().setValue(new ChatMessage(input.getText().toString(),
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()));
                input.setText("");
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        /*adapter.startListening();*/
        user = FirebaseAuth.getInstance().getCurrentUser();
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
       /* adapter.stopListening();*/

        super.onPause();
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

    private void authTest() {
        if (user != null) {
            Snackbar.make(activity_main, getString(R.string.greeting) + user.getDisplayName(), Snackbar.LENGTH_SHORT).show();
            displayChat();
        } else {
            requestSignIn();
            displayChat();
        }
    }

    /* db output */
    private void displayChat() {
        ListView listOfMessage = findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.list_item, FirebaseDatabase.getInstance().getReference().child((MESSAGES_CHILD))) {

            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText;
                TextView messageUser;
                TextView messageTime;
                messageText = v.findViewById(R.id.message_text);
                messageUser = v.findViewById(R.id.message_user);
                messageTime = v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("h:mm a", model.getMessageTime()));
            }
        };
        listOfMessage.setAdapter(adapter);

    }

    private void requestSignIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .build(),
                SIGN_IN_REQUEST_CODE);
    }

    private void onCreateDrawer(FirebaseUser user) {
        final ProfileDrawerItem profile = new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl());
//        final ProfileDrawerItem profileStatic = new ProfileDrawerItem().withName("name").withEmail("email").withIcon(getResources().getDrawable(R.drawable.ic_action_account_circle))
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withIcon(R.drawable.ic_action_home).withName(R.string.drawer_item_home).withSelectable(false);
        final SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withIcon(R.drawable.ic_action_settings).withDescription(R.string.settings_description).withName(R.string.drawer_item_settings).withSelectable(false);
        final SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withIcon(R.drawable.ic_menu_share).withName(R.string.drawer_item_invitation).withSelectable(false);
        final SwitchDrawerItem item4 = new SwitchDrawerItem().withIdentifier(4).withIcon(R.drawable.ic_menu_slideshow).withName(R.string.drawer_item_slideshow).withChecked(true);

        Toolbar toolbar = findViewById(R.id.toolbar);

        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.side_nav_bar)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(R.drawable.ic_action_account_circle).withIdentifier(100001)
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
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3,
                        item4
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // TODO: on tap
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
//                                intent = new Intent(MainActivity.this, CompactHeaderDrawerActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {

                            } else if (drawerItem.getIdentifier() == 3) {
                                sendInvitation();
                            }

                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .withAccountHeader(drawerHeader)
                .build();

    }

    /* add the values to the bundle */
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
        startActivityForResult(intent, REQUEST_INVITE);
    }

}
