package firebase.balancechat.util;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class LoadImage {
    public static void loadImages(StorageReference storageRef, final ImageView imageView) {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
//                Picasso.get().load(uri.toString()).ic_imageoff(R.drawable.ic_imageoff).resize(200, 200).centerCrop().transform(new CircleTransform()).into(imageView);
                Picasso.get().load(uri.toString()).resize(200, 200).centerCrop().transform(new CircleTransform()).into(imageView);
            }
        });
    }
}
