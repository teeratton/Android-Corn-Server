package tmt.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;


//Thread
public class MainActivity extends AppCompatActivity {

    Uri imageUri;
    private Bitmap imageBitmap;
    private ImageView imageView;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(connectListener);
        Button btnImage = findViewById(R.id.btnImage);
        imageView = findViewById(R.id.imageView);
        btnImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openGallery();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageURI(imageUri);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();

        }
    }
    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, PICK_IMAGE);
    }

    private View.OnClickListener connectListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Thread cThread = new Thread(new ClientThread());
            cThread.start();
        }

    };

    private class ClientThread implements Runnable {

        Bitmap resized = Bitmap.createScaledBitmap(imageBitmap, 540, 720, true);
        String imgString = Base64.encodeToString(getBytesFromBitmap(resized), Base64.NO_WRAP);
        String imgStringSize = Integer.toString(imgString.length());

        private int decodeString(byte[] bytes) throws UnsupportedEncodingException {
            String s = new String(bytes, "UTF-8");
            String result = "";
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (Character.isDigit(c)) result += c;
            }
            return Integer.parseInt(result);
        }
        public void run() {
            try {
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket("10.45.128.157", 8889);
                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    out.write(imgStringSize.getBytes());
                    byte[] sizeByte = new byte[100];
                    in.read(sizeByte);
                    int byteLeft = decodeString(sizeByte);
                    int count = 0;
                    int sent = 0;
                    while (byteLeft > 0) {
                        out.write(imgString.substring(count, count + Math.min(byteLeft, 1024)).getBytes());
                        if (byteLeft < 1024) break;

                        byteLeft -= 1024;
                        count += 1024;
                    }
                    Log.d("ClientActivity", "C: Sent.");
                } catch (Exception e) {
                    Log.e("ClientActivity", "S: Error", e);
                }
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
            }
        }
        public byte[] getBytesFromBitmap(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            return stream.toByteArray();
        }
    }
}




