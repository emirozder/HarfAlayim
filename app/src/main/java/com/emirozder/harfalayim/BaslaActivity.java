package com.emirozder.harfalayim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class BaslaActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    private Cursor cursor;
    private ArrayList<String> sorularList;
    private ArrayList<String> sorularKodList;
    private ArrayList<String> kelimelerList;
    private ArrayList<Character> kelimeHarfleri;
    private String[] txt_cevapHarfler;
    private char[] gelenKelimeHarfler;
    private Random rndSoru, rndKelime, rndHarf;
    private int rndSoruNumber, rndKelimeNumber, rndHarfNumber;
    private String rastgeleSoru, rastgeleSoruKodu, rastgeleKelime, kelimeBilgisi, et_kullaniciTahmin, et_kullaniciTahminSubstring, et_kullaniciTahminLowerCase, rastgeleKelimeLowerCase;
    private int rastgeleBelirlenecekHarfSayisi;
    private int canSayisi = 5;

    private TextView txt_soru;
    private TextView txt_cevap;
    private TextView txt_canSayisi;
    private TextView txt_category;
    private TextView txt_bestScore;
    private EditText et_tahmin;
    private Button btn_harfalayim;
    private Button btn_pas;
    private Button btn_benjamin;
    private Button btn_evetBA, btn_hayirBA;

    private Dialog statisticDialog, backpressedDialog;
    private TextView txt_wordCountStats, txt_trueWordCountStats, txt_falseWordCountStats, txt_complatedCategoryCountStats, txt_statsBestScore;
    private ProgressBar cd_stats_progressbarWordCount, cd_stats_progressbarTrueWordCount, cd_stats_progressbarFalseWordCount, cd_stats_progressbarcomplatedCategoryCount;
    private Button statsCD_btn_anamenu, statsCD_btn_tekraroyna;
    int dogruBilinenKelimeSayisi = 0, yanlisBilinenKelimeSayisi = 0, tamamlananKategoriSayisi = 0, maxSoruSayisi = 0, maxKelimeSayisi = 0, acilanKelimeSayisi = 0;

    private Thread thread;
    private LottieAnimationView trueAnimView, falseAnimView, passAnimView;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int getBestScore;

    private void init(){
        sorularList = new ArrayList<>();
        sorularKodList = new ArrayList<>();
        kelimelerList = new ArrayList<>();
        kelimeHarfleri = new ArrayList<>();
        rndSoru = new Random();
        rndKelime = new Random();
        rndHarf = new Random();
        txt_canSayisi = (TextView) findViewById(R.id.txt_canSayisi);
        txt_canSayisi.setText("+5");
        txt_soru = (TextView) findViewById(R.id.txt_soru);
        txt_cevap = (TextView) findViewById(R.id.txt_cevap);
        txt_category = (TextView) findViewById(R.id.txt_category);
        txt_bestScore = (TextView) findViewById(R.id.txt_bestScore);
        et_tahmin = (EditText) findViewById(R.id.et_tahmin);
        btn_harfalayim = (Button) findViewById(R.id.btn_harfalayim);
        btn_pas = (Button) findViewById(R.id.btn_pas);
        btn_benjamin = (Button) findViewById(R.id.btn_benjamin);
        trueAnimView = (LottieAnimationView) findViewById(R.id.true_animView);
        falseAnimView = (LottieAnimationView) findViewById(R.id.false_animView);
        passAnimView = (LottieAnimationView) findViewById(R.id.pass_animView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basla);
        init();

        preferences = this.getSharedPreferences("com.emirozder.harfalayim", MODE_PRIVATE);
        getBestScore = preferences.getInt("bestScore", 0);
        txt_bestScore.setText(String.valueOf(getBestScore));

        for (Map.Entry soru : SplashScreenActivity.sorularHashMap.entrySet()){
            sorularList.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

        rastgeleSoruGetir();
    }

    public void btn_harfAlayimClick(View v){
        if (canSayisi > 0 && kelimeBilgisi.contains("_")){
            rastgeleHarfAl();
            canSayisi--;
            txt_canSayisi.setText("+" + String.valueOf(canSayisi));
            if (canSayisi == 0){
                Toast.makeText(getApplicationContext(), "Harf alma hakkınız bitmiştir.", Toast.LENGTH_SHORT).show();
                btn_harfalayim.setClickable(false);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Bütün harfler açılmıştır.\nDaha fazla harf alamazsınız.", Toast.LENGTH_SHORT).show();
            btn_harfalayim.setClickable(false);
        }
    }

    public void btn_pasClick(View v){
        btn_harfalayim.setClickable(true);
        canSayisi--;
        if (canSayisi > 0){
            Toast.makeText(getApplicationContext(), "Pas Geçildi!\nKalan hak sayısı " + canSayisi + ".", Toast.LENGTH_SHORT).show();
            txt_canSayisi.setText("+" + String.valueOf(canSayisi));
            et_tahmin.setText("");
            txt_cevap.setText("");
            passAnimView.setVisibility(View.VISIBLE);
            passAnimView.playAnimation();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1300);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                passAnimView.cancelAnimation();
                                passAnimView.setVisibility(View.INVISIBLE);
                            }
                        });
                        if (kelimelerList.size() > 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rastgeleKelimeGetir();
                                }
                            });
                        }
                        else{
                            if (sorularList.size() > 0){
                                tamamlananKategoriSayisi++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rastgeleSoruGetir();
                                    }
                                });
                            }
                            else{
                                tamamlananKategoriSayisi++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        maxVeriSayilariniHesapla();
                                        Toast.makeText(getApplicationContext(), "Sorular bitti!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }else{
            Toast.makeText(getApplicationContext(), "Pas Geçildi!\nTahmin hakkınız kalmamıştır. Oyun Bitti!", Toast.LENGTH_SHORT).show();
            if (canSayisi==0){
                txt_canSayisi.setText("+" + String.valueOf(canSayisi));
            }
            et_tahmin.setText("");
            txt_cevap.setText("");
            passAnimView.setVisibility(View.VISIBLE);
            passAnimView.playAnimation();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1300);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                passAnimView.cancelAnimation();
                                passAnimView.setVisibility(View.INVISIBLE);
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                maxVeriSayilariniHesapla();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    public void btn_benjaminClick(View v){
        btn_harfalayim.setClickable(true);
        et_kullaniciTahmin = et_tahmin.getText().toString().trim();
        if (!TextUtils.isEmpty(et_kullaniciTahmin)){
            et_kullaniciTahminSubstring = et_kullaniciTahmin.substring(0,1).toUpperCase() + et_kullaniciTahmin.substring(1);
            rastgeleKelimeLowerCase = rastgeleKelime.toLowerCase();
            et_kullaniciTahminLowerCase = et_kullaniciTahmin.toLowerCase();
            //System.out.println("tahminuppercase: " + et_kullaniciTahminSubstring);
            //System.out.println("rastgelekelimelowercase: " + rastgeleKelimeLowerCase);
            if (et_kullaniciTahmin.matches(rastgeleKelime) || et_kullaniciTahminSubstring.matches(rastgeleKelime) || et_kullaniciTahmin.matches(rastgeleKelimeLowerCase)
                    || et_kullaniciTahminSubstring.matches(rastgeleKelimeLowerCase) || et_kullaniciTahminLowerCase.matches(rastgeleKelimeLowerCase)){
                Toast.makeText(getApplicationContext(), "Doğru Tahmin!", Toast.LENGTH_SHORT).show();
                dogruBilinenKelimeSayisi++;
                canSayisi++;
                txt_canSayisi.setText("+" + String.valueOf(canSayisi));
                et_tahmin.setText("");
                txt_cevap.setText("");
                trueAnimView.setVisibility(View.VISIBLE);
                trueAnimView.playAnimation();
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    trueAnimView.cancelAnimation();
                                    trueAnimView.setVisibility(View.INVISIBLE);
                                }
                            });
                            if (kelimelerList.size() > 0){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rastgeleKelimeGetir();
                                    }
                                });
                            }
                            else{
                                if (sorularList.size() > 0){
                                    tamamlananKategoriSayisi++;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            rastgeleSoruGetir();
                                        }
                                    });
                                }
                                else{
                                    tamamlananKategoriSayisi++;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            maxVeriSayilariniHesapla();
                                            Toast.makeText(getApplicationContext(), "Sorular bitti!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
            else{
                canSayisi--;
                yanlisBilinenKelimeSayisi++;
                if (canSayisi > 0){
                    Toast.makeText(getApplicationContext(), "Yanlış Tahmin!\nKalan hak sayısı " + canSayisi + ".", Toast.LENGTH_SHORT).show();
                    txt_canSayisi.setText("+" + String.valueOf(canSayisi));
                    et_tahmin.setText("");
                    txt_cevap.setText("");
                    falseAnimView.setVisibility(View.VISIBLE);
                    falseAnimView.playAnimation();
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        falseAnimView.cancelAnimation();
                                        falseAnimView.setVisibility(View.INVISIBLE);
                                    }
                                });
                                if (kelimelerList.size() > 0){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            rastgeleKelimeGetir();
                                        }
                                    });
                                }
                                else{
                                    if (sorularList.size() > 0){
                                        tamamlananKategoriSayisi++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                rastgeleSoruGetir();
                                            }
                                        });
                                    }
                                    else{
                                        tamamlananKategoriSayisi++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                maxVeriSayilariniHesapla();
                                                Toast.makeText(getApplicationContext(), "Sorular bitti!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }else{
                    Toast.makeText(getApplicationContext(), "Yanlış Tahmin!\nTahmin hakkınız kalmamıştır. Oyun Bitti!", Toast.LENGTH_SHORT).show();
                    if (canSayisi==0){
                        txt_canSayisi.setText("+" + String.valueOf(canSayisi));
                    }
                    et_tahmin.setText("");
                    txt_cevap.setText("");
                    falseAnimView.setVisibility(View.VISIBLE);
                    falseAnimView.playAnimation();
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        falseAnimView.cancelAnimation();
                                        falseAnimView.setVisibility(View.INVISIBLE);
                                    }
                                });
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        maxVeriSayilariniHesapla();
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Lütfen bir tahminde bulunun.", Toast.LENGTH_SHORT).show();
        }
    }

    private void rastgeleSoruGetir(){
        rndSoruNumber = rndSoru.nextInt(sorularKodList.size());
        rastgeleSoru = sorularList.get(rndSoruNumber);
        sorularList.remove(rndSoruNumber);
        rastgeleSoruKodu = sorularKodList.get(rndSoruNumber);
        sorularKodList.remove(rndSoruNumber);
        txt_soru.setText(rastgeleSoru);
        switch (rastgeleSoruKodu){
            case "agaclar1":
                txt_category.setText("Kategori: Ağaçlar");
                break;
            case "aracgerec1":
                txt_category.setText("Kategori: Araç-Gereç");
                break;
            case "baskentler1":
                txt_category.setText("Kategori: Başkentler");
                break;
            case "cicekler1":
                txt_category.setText("Kategori: Çiçekler");
                break;
            case "yemekler1":
                txt_category.setText("Kategori: Yemek");
                break;
            case "sporlar1":
                txt_category.setText("Kategori: Spor");
                break;
            case "turistik1":
                txt_category.setText("Kategori: Turistik Yerler");
                break;
            case "ulkeler1":
                txt_category.setText("Kategori: Ülkeler");
                break;
            case "muzikgruplari1":
                txt_category.setText("Kategori: Müzik");
                break;
            case "elementler1":
                txt_category.setText("Kategori: Elementler");
                break;
        }

        try {
            database = this.openOrCreateDatabase("kelimeDB",MODE_PRIVATE,null);
            cursor = database.rawQuery("SELECT * FROM Kelimeler WHERE kKod = ?", new String[]{rastgeleSoruKodu});
            int kelimeIndex = cursor.getColumnIndex("kelime");
            while (cursor.moveToNext()){
                kelimelerList.add(cursor.getString(kelimeIndex));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        rastgeleKelimeGetir();
    }

    private void rastgeleHarfAl(){
        if (kelimeHarfleri.size() > 0){
            rndHarfNumber = rndHarf.nextInt(kelimeHarfleri.size());
            System.out.println("gelen harf: " + kelimeHarfleri.get(rndHarfNumber));
            txt_cevapHarfler = txt_cevap.getText().toString().split(" ");
            gelenKelimeHarfler = rastgeleKelime.toCharArray();
            for (int i = 0; i < rastgeleKelime.length(); i++){
                if (txt_cevapHarfler[i].equals("_") && gelenKelimeHarfler[i] == kelimeHarfleri.get(rndHarfNumber)){
                    txt_cevapHarfler[i] = String.valueOf(kelimeHarfleri.get(rndHarfNumber));
                    kelimeBilgisi="";

                    for (int j = 0; j < txt_cevapHarfler.length; j++){
                        if (j < txt_cevapHarfler.length - 1)
                            kelimeBilgisi += txt_cevapHarfler[j] + " ";
                        else
                            kelimeBilgisi += txt_cevapHarfler[j];
                    }
                    break;
                }
            }
            txt_cevap.setText(kelimeBilgisi);
            kelimeHarfleri.remove(rndHarfNumber);
        }
    }

    private void rastgeleKelimeGetir(){
        kelimeBilgisi="";
        kelimeHarfleri.clear();
        rndKelimeNumber = rndKelime.nextInt(kelimelerList.size());
        rastgeleKelime = kelimelerList.get(rndKelimeNumber);
        kelimelerList.remove(rndKelimeNumber);
        acilanKelimeSayisi++;

        System.out.println("Gelen Kelime= " + rastgeleKelime); //*************************************************
        for (char harf : rastgeleKelime.toCharArray()) {
            kelimeHarfleri.add(harf);
        }

        for (int i = 0; i < rastgeleKelime.length(); i++){
            if (i < rastgeleKelime.length() - 1 && !kelimeHarfleri.get(i).equals(' ')){
                kelimeBilgisi += "_ ";
            }
            else if (i < rastgeleKelime.length() - 1 && kelimeHarfleri.get(i).equals(' ')){
                kelimeBilgisi += " ";
            }
            else{
                kelimeBilgisi += "_";
            }
        }

        txt_cevap.setText(kelimeBilgisi);

        if (rastgeleKelime.length() >= 5 && rastgeleKelime.length() <= 7){
            rastgeleBelirlenecekHarfSayisi = 1;
        }
        else if (rastgeleKelime.length() >=8 && rastgeleKelime.length() <= 10){
            rastgeleBelirlenecekHarfSayisi = 2;
        }
        else if (rastgeleKelime.length() >=11 && rastgeleKelime.length() <= 14) {
            rastgeleBelirlenecekHarfSayisi = 3;
        }
        else if (rastgeleKelime.length() >=15) {
            rastgeleBelirlenecekHarfSayisi = 4;
        }
        else{
            rastgeleBelirlenecekHarfSayisi = 0;
        }

        for (int i = 0; i < rastgeleBelirlenecekHarfSayisi; i++){
            rastgeleHarfAl();
        }
    }

    @SuppressLint("SetTextI18n")
    private void istatistikTablosuGoster(int dogruBilinenKelimeSayisi, int yanlisBilinenKelimeSayisi, int tamamlananKategoriSayisi, int maxKelimeSayisi, int maxSoruSayisi, int acilanKelimeSayisi, int getBestScore){
        statisticDialog = new Dialog(this);
        statisticDialog.setContentView(R.layout.statistics_custom_dialog);
        statisticDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        statisticDialog.setCancelable(false);

        txt_wordCountStats = (TextView) statisticDialog.findViewById(R.id.txt_wordCountStats);
        txt_trueWordCountStats = (TextView) statisticDialog.findViewById(R.id.txt_trueWordCountStats);
        txt_statsBestScore = (TextView) statisticDialog.findViewById(R.id.txt_statsBestScore);
        txt_falseWordCountStats = (TextView) statisticDialog.findViewById(R.id.txt_falseWordCountStats);
        txt_complatedCategoryCountStats = (TextView) statisticDialog.findViewById(R.id.txt_complatedCategoryCountStats);
        cd_stats_progressbarWordCount = (ProgressBar) statisticDialog.findViewById(R.id.cd_stats_progressbarWordCount);
        cd_stats_progressbarTrueWordCount = (ProgressBar) statisticDialog.findViewById(R.id.cd_stats_progressbarTrueWordCount);
        cd_stats_progressbarFalseWordCount = (ProgressBar) statisticDialog.findViewById(R.id.cd_stats_progressbarFalseWordCount);
        cd_stats_progressbarcomplatedCategoryCount = (ProgressBar) statisticDialog.findViewById(R.id.cd_stats_progressbarcomplatedCategoryCount);
        statsCD_btn_anamenu = (Button) statisticDialog.findViewById(R.id.statsCD_btn_anamenu);
        statsCD_btn_tekraroyna = (Button) statisticDialog.findViewById(R.id.statsCD_btn_tekraroyna);

        txt_wordCountStats.setText(acilanKelimeSayisi + " / " + maxKelimeSayisi);
        txt_trueWordCountStats.setText(dogruBilinenKelimeSayisi + " / " + acilanKelimeSayisi);
        txt_statsBestScore.setText("En İyi Skor: " + getBestScore);
        txt_falseWordCountStats.setText(yanlisBilinenKelimeSayisi + " / " + acilanKelimeSayisi);
        txt_complatedCategoryCountStats.setText(tamamlananKategoriSayisi + " / " + maxSoruSayisi);

        cd_stats_progressbarWordCount.setMax(maxKelimeSayisi);
        cd_stats_progressbarWordCount.setProgress(acilanKelimeSayisi);
        cd_stats_progressbarTrueWordCount.setMax(acilanKelimeSayisi);
        cd_stats_progressbarTrueWordCount.setProgress(dogruBilinenKelimeSayisi);
        cd_stats_progressbarFalseWordCount.setMax(acilanKelimeSayisi);
        cd_stats_progressbarFalseWordCount.setProgress(yanlisBilinenKelimeSayisi);
        cd_stats_progressbarcomplatedCategoryCount.setMax(maxSoruSayisi);
        cd_stats_progressbarcomplatedCategoryCount.setProgress(tamamlananKategoriSayisi);

        statsCD_btn_anamenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainIntent();
            }
        });

        statsCD_btn_tekraroyna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tekrarOynaIntent = new Intent(BaslaActivity.this, BaslaActivity.class);
                finish();
                startActivity(tekrarOynaIntent);
            }
        });

        statisticDialog.show();
    }

    private void maxVeriSayilariniHesapla(){
        try {
            cursor = database.rawQuery("SELECT COUNT (*) FROM Kelimeler",null);
            cursor.moveToNext();
            maxKelimeSayisi = cursor.getInt(0);

            cursor = database.rawQuery("SELECT COUNT (*) FROM Sorular",null);
            cursor.moveToNext();
            maxSoruSayisi = cursor.getInt(0);

            cursor.close();

            if (dogruBilinenKelimeSayisi > getBestScore){
                getBestScore = dogruBilinenKelimeSayisi;
            }
            editor = preferences.edit();
            editor.putInt("bestScore", getBestScore);
            editor.apply();

            istatistikTablosuGoster(dogruBilinenKelimeSayisi, yanlisBilinenKelimeSayisi, tamamlananKategoriSayisi, maxKelimeSayisi, maxSoruSayisi, acilanKelimeSayisi, getBestScore);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        backpressedDialog = new Dialog(this);
        backpressedDialog.setContentView(R.layout.backpressed_custom_dialog);
        backpressedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        backpressedDialog.setCancelable(false);

        btn_evetBA = (Button) backpressedDialog.findViewById(R.id.backpressed_cd_btn_evet);
        btn_hayirBA = (Button) backpressedDialog.findViewById(R.id.backpressed_cd_btn_hayir);

        btn_evetBA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxVeriSayilariniHesapla();
            }
        });

        btn_hayirBA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backpressedDialog.dismiss();
            }
        });

        backpressedDialog.show();
    }

    private void mainIntent(){
        Intent mainIntent = new Intent(BaslaActivity.this,MainActivity.class);
        finish();
        startActivity(mainIntent);
    }
}