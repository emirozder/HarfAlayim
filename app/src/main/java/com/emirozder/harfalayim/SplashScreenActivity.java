package com.emirozder.harfalayim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView txt_progressbar;
    private SQLiteDatabase database;
    private Cursor cursor;
    static public HashMap<String,String> sorularHashMap;
    private String sqlSorgusu;
    private SQLiteStatement statement;
    private int counter=0;
    private Thread thread;
    private Handler handler;

    //Sorular için listler
    private String[] sorularListDizi = {"Ülkemizin iklim ve coğrafyasında görülen ağaç türleri?", "Bir takım çantasında bulunabilecek araç - gereçler?",
            "Avrupa kıtasında bulunan ülkelerin başkentleri?", "Bir çiçekçide görülebilecek çiçek çeşitleri?", "Türk mutfağının önde gelen yemekleri?", "Olimpiyat oyunlarında yer alan sporlar?",
            "Türkiye'de bulunan turistik yerler?", "Güney Amerika kıtasında bulunan ülkeler?", "Ünlü Türk müzik grupları?" , "Periyodik tabloda ilk 20'de bulunan elementler?"};
    private String[] sorularKodListDizi = {"agaclar1", "aracgerec1", "baskentler1", "cicekler1", "yemekler1", "sporlar1", "turistik1", "ulkeler1", "muzikgruplari1", "elementler1"};

    //Kelimeler için listeler
    private String[] kelimelerListDizi = {"Sedir", "Gürgen", "Meşe", "Kayın", "Çam", "Kavak", "Çınar", "Dişbudak", "Ladin", "Ardıç",
            "Çekiç", "Tornavida", "Alyan", "İngiliz Anahtarı", "Kargaburun", "Pense", "Maket Bıçağı", "Çivi", "Vida", "Dübel",
            "Helsinki", "Oslo", "Madrid", "Prag", "Lizbon", "Zagreb", "Budapeşte", "Brüksel", "Amsterdam", "Viyana",
            "Açelya", "Kamelya", "Sümbül", "Karanfil", "Gül", "Kasımpatı", "Lavanta", "Menekşe", "Orkide", "Begonya",
            "İskender", "Kebap", "Döner", "İçli Köfte", "Mantı", "Lahmacun", "Pide", "Baklava", "Cacık", "Karnıyarık",
            "Atletizm", "Judo", "Okçuluk", "Halter", "Yüzme", "Karate", "Güreş", "Basketbol", "Eskrim", "Jimnastik",
            "Pamukkale", "Kapadokya", "Efes", "Ayasofya", "Göbeklitepe", "Safranbolu", "Anıtkabir", "Kapalıçarşı", "Uzungöl" , "Sultanahmet",
            "Peru", "Arjantin", "Brezilya", "Şili", "Kolombiya", "Uruguay", "Ekvador", "Venezuela", "Paraguay", "Bolivya",
            "Manga", "Mor ve Ötesi", "Seksendört", "Pinhani", "Duman", "Athena", "Model", "Gripin", "Yüksek Sadakat", "Kolpa",
            "Argon", "Silisyum", "Magnezyum", "Sodyum", "Kükürt", "Azot", "Flor", "Berilyum", "Helyum", "Kalsiyum"};
    private String[] kelimelerKodListDizi = {"agaclar1","agaclar1","agaclar1","agaclar1","agaclar1","agaclar1","agaclar1","agaclar1","agaclar1","agaclar1",
            "aracgerec1", "aracgerec1","aracgerec1","aracgerec1","aracgerec1","aracgerec1","aracgerec1","aracgerec1","aracgerec1","aracgerec1",
            "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1", "baskentler1",
            "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1", "cicekler1",
            "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1", "yemekler1",
            "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1", "sporlar1",
            "turistik1", "turistik1", "turistik1", "turistik1", "turistik1", "turistik1", "turistik1", "turistik1", "turistik1", "turistik1",
            "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1", "ulkeler1",
            "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1", "muzikgruplari1",
            "elementler1", "elementler1", "elementler1", "elementler1", "elementler1", "elementler1", "elementler1", "elementler1", "elementler1", "elementler1"};

    private void init(){
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        txt_progressbar = (TextView) findViewById(R.id.txt_progressbar);
        sorularHashMap = new HashMap<>();
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        try {
            database = this.openOrCreateDatabase("kelimeDB",MODE_PRIVATE,null);

            database.execSQL("CREATE TABLE IF NOT EXISTS Sorular (soruID INTEGER PRIMARY KEY, sKod VARCHAR UNIQUE, soru VARCHAR)");
            database.execSQL("DELETE FROM Sorular");
            dbSoruEkle();

            database.execSQL("CREATE TABLE IF NOT EXISTS Kelimeler (kKod VARCHAR, kelime VARCHAR, FOREIGN KEY (kKod) REFERENCES Sorular (sKod))");
            database.execSQL("DELETE FROM Kelimeler");
            dbKelimeEkle();

            cursor = database.rawQuery("SELECT * FROM Sorular",null);

            int sKodIndex = cursor.getColumnIndex("sKod");
            int soruIndex = cursor.getColumnIndex("soru");


            while (cursor.moveToNext()){
                sorularHashMap.put(cursor.getString(sKodIndex), cursor.getString(soruIndex));
            }

            cursor.close();

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startProgress();
                }
            });
            thread.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startProgress(){
        for (counter=0;counter<=100;counter++){
            try {
                Thread.sleep(60);//60
                progressBar.setProgress(counter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (counter < 100)
                        txt_progressbar.setText("Sorular yükleniyor... " + " %" + String.valueOf(counter));
                    else
                        txt_progressbar.setText("Sorular yüklendi, Benjamin Hazır!");
                }
            });
        }
        try {
            Thread.sleep(1300);//1300
            Intent mainIntent = new Intent(SplashScreenActivity.this,MainActivity.class);
            finish();
            startActivity(mainIntent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dbSoruEkle(){
        try {
            for (int s = 0; s < sorularListDizi.length; s++){
                sqlSorgusu = "INSERT INTO Sorular (sKod,soru) VALUES (?,?)";
                statement = database.compileStatement(sqlSorgusu);
                statement.bindString(1,sorularKodListDizi[s]);
                statement.bindString(2,sorularListDizi[s]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();;
        }
    }

    private void dbKelimeEkle(){
        try {
            for (int k = 0; k < kelimelerListDizi.length; k++){
                sqlSorgusu = "INSERT INTO Kelimeler (kKod,kelime) VALUES (?,?)";
                statement = database.compileStatement(sqlSorgusu);
                statement.bindString(1,kelimelerKodListDizi[k]);
                statement.bindString(2,kelimelerListDizi[k]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();;
        }
    }
}