package com.emirozder.harfalayim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private Dialog settingsDialog, exitDialog, aboutDialog;
    private ImageView imgView_profilePhoto, imgView_profile_cd;
    private EditText et_name;
    private Button btn_kaydet, btn_cikis, btn_evet, btn_hayir, btn_kapat;
    private TextView txt_name;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String username = "Oyuncu", getUsername, photo, previouslyPhoto;
    private int izinVerme = 0, izinVerildi = 1, width, height;
    private double bitmapOrani;
    private Intent galeriIntent;
    private Uri gorselUri;
    private Bitmap gorselBitmap, gelenGorselBitmap, kucukBitmap;
    private ImageDecoder.Source gorselSource;
    private byte[] gorselByte, gelenGorselByte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_name = (TextView) findViewById(R.id.txt_name);
        imgView_profilePhoto = (ImageView) findViewById(R.id.imgView_profilePhoto);

        preferences = this.getSharedPreferences("com.emirozder.harfalayim", MODE_PRIVATE);
        getUsername = preferences.getString("username", "Oyuncu");
        if (!TextUtils.isEmpty(getUsername)) {
            txt_name.setText(getUsername);
        }

        previouslyPhoto = preferences.getString("imageData", "");
        if (!previouslyPhoto.equalsIgnoreCase("")) {
            gelenGorselByte = Base64.decode(previouslyPhoto, Base64.DEFAULT);
            gelenGorselBitmap = BitmapFactory.decodeByteArray(gelenGorselByte, 0, gelenGorselByte.length);
            imgView_profilePhoto.setImageBitmap(gelenGorselBitmap);
        }
    }

    public void mainBtnClick(View v) {
        switch (v.getId()) {
            case R.id.btn_basla:
                Intent baslaIntent = new Intent(this, BaslaActivity.class);
                finish();
                startActivity(baslaIntent);
                break;

            case R.id.btn_hakkinda:
                aboutDialogGoster();
                break;

            case R.id.btn_cikis:
                cikisDialogGoster();
                break;
        }
    }

    public void btn_settings(View v) {
        ayarlarDialogGoster();
    }

    public void ayarlarDialogGoster() {
        settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.settings_custom_dialog);
        settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        settingsDialog.setCancelable(false);


        imgView_profile_cd = (ImageView) settingsDialog.findViewById(R.id.cd_imgView_profile);
        et_name = (EditText) settingsDialog.findViewById(R.id.et_settingsCD_name);
        btn_kaydet = (Button) settingsDialog.findViewById(R.id.settingsCD_btn_kaydet);
        btn_cikis = (Button) settingsDialog.findViewById(R.id.settingsCD_btn_cikis);
        et_name.setText(txt_name.getText().toString());


        previouslyPhoto = preferences.getString("imageData", "");
        if (!previouslyPhoto.equalsIgnoreCase("")) {
            gelenGorselByte = Base64.decode(previouslyPhoto, Base64.DEFAULT);
            gelenGorselBitmap = BitmapFactory.decodeByteArray(gelenGorselByte, 0, gelenGorselByte.length);
            imgView_profile_cd.setImageBitmap(gelenGorselBitmap);
        }


        btn_kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    isimDegistir();
                    kucukBitmap = kucukBitmapOlustur(gorselBitmap, 300);
                    photoKaydet(kucukBitmap);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        btn_cikis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
            }
        });

        imgView_profile_cd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profilfotoDegistir();
            }
        });

        settingsDialog.show();
    }

    public void isimDegistir() {
        username = et_name.getText().toString().trim();
        if (!TextUtils.isEmpty(username)) {
            editor = preferences.edit();
            editor.putString("username", username);
            editor.apply();
            txt_name.setText(username);
            settingsDialog.dismiss();
        } else {
            Toast.makeText(getApplicationContext(), "Lütfen bir kullanıcı adı giriniz.", Toast.LENGTH_SHORT).show();
        }
    }

    public void profilfotoDegistir() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, izinVerme);
        } else {
            galeriIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galeriIntent, izinVerildi);
        }
    }

    //izin alınmamışsa; izin al, galeriye öyle git.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == izinVerme) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galeriIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galeriIntent, izinVerildi);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //zaten izin alınmışsa; tekrar izin almadan galeriye git.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == izinVerildi) {
            if (resultCode == RESULT_OK && data != null) {
                gorselUri = data.getData();
                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        gorselSource = ImageDecoder.createSource(this.getContentResolver(), gorselUri);
                        gorselBitmap = ImageDecoder.decodeBitmap(gorselSource);
                        imgView_profile_cd.setImageBitmap(gorselBitmap);
                    } else {
                        gorselBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), gorselUri);
                        imgView_profile_cd.setImageBitmap(gorselBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void photoKaydet(Bitmap profilePhoto) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            profilePhoto.compress(Bitmap.CompressFormat.PNG, 75, outputStream);
            gorselByte = outputStream.toByteArray();

            photo = Base64.encodeToString(gorselByte, Base64.DEFAULT);
            editor = preferences.edit();
            editor.putString("imageData", photo);
            editor.commit();

            imgView_profilePhoto.setImageBitmap(profilePhoto);
            imgView_profile_cd.setImageBitmap(profilePhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap kucukBitmapOlustur(Bitmap secilenBitmap, int maxBoyut) {
        width = secilenBitmap.getWidth();
        height = secilenBitmap.getHeight();
        bitmapOrani = Double.valueOf(width) / Double.valueOf(height);
        if (bitmapOrani > 1) {
            //secilen gorsel yatay
            width = maxBoyut;
            double kisaltilmisHeight = width / bitmapOrani;
            height = (int) kisaltilmisHeight;
        } else {
            //secilen gorsel dikey
            height = maxBoyut;
            double kisaltilmisWidth = height * bitmapOrani;
            width = (int) kisaltilmisWidth;
        }
        return Bitmap.createScaledBitmap(secilenBitmap, width, height, true);
    }

    private void cikisDialogGoster(){
        exitDialog = new Dialog(this);
        exitDialog.setContentView(R.layout.exit_custom_dialog);
        exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        exitDialog.setCancelable(false);

        btn_evet = (Button) exitDialog.findViewById(R.id.exit_cd_btn_evet);
        btn_hayir = (Button) exitDialog.findViewById(R.id.exit_cd_btn_hayir);

        btn_evet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });

        btn_hayir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }

    private void aboutDialogGoster(){
        aboutDialog = new Dialog(this);
        aboutDialog.setContentView(R.layout.about_custom_dialog);
        aboutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        aboutDialog.setCancelable(false);

        btn_kapat = (Button) aboutDialog.findViewById(R.id.aboutCD_btn_kapat);

        btn_kapat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutDialog.dismiss();
            }
        });

        aboutDialog.show();
    }

    @Override
    public void onBackPressed() {
        cikisDialogGoster();
    }
}