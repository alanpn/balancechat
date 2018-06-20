package firebase.balancechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Element adsElement = new Element();
//        adsElement.setTitle("Balance Chat");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("Balance Chat")
                .setImage(R.drawable.balance_launcher)
                .addItem(new Element().setTitle("Version 1.1"))
                .addGroup("Connect with us")
                .addEmail("denisrondalev@gmail.com")
                .addTwitter("standvirgin")
                .addGitHub("lmmamercy")
                .create();

        setContentView(aboutPage);
    }


}
