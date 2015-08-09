package com.ka34.nit_ibc_information;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    final String version = "0.1";
    String newVersion;
    HTMLparser parse = new HTMLparser();
    private UserSetting userSetting;
    String[] my;
    CustomAdapter customAdapater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(HTMLparser.infoData==null) {
            if (isConnected(this.getApplicationContext())) {
                final String url = "http://www.ibaraki-ct.ac.jp/?page_id=501";
                AsyncHttpRequest task = new AsyncHttpRequest(this);
                task.execute(url);
                userSetting = UserSetting.getInstance(getApplicationContext());
                if (userSetting.grade == null) {
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.alert_dialog, (ViewGroup) findViewById(R.id.alertdialog_layout));
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("所属の設定");
                    builder.setView(layout);
                    final Spinner spgrade = (Spinner) layout.findViewById(R.id.spgrade);
                    final Spinner spdep = (Spinner) layout.findViewById(R.id.spdep);
                    final Spinner spclass = (Spinner) layout.findViewById(R.id.spclass);
                    final CheckBox checkabroiad = (CheckBox) layout.findViewById(R.id.abroad);
                    builder.setPositiveButton("おーけー", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // おーけー ボタンクリック処理
                            userSetting = UserSetting.getInstance(getApplicationContext());
                            userSetting.grade = (String) spgrade.getSelectedItem();
                            userSetting.dep = (String) spdep.getSelectedItem();
                            userSetting.clas = (String) spclass.getSelectedItem();
                            if (checkabroiad.isChecked()){
                                userSetting.abroad = "留学生";
                            } else {
                                userSetting.abroad = null;
                            }
                            userSetting.savaInstance(getApplicationContext());
                            parse.GetfilterList(userSetting.grade,userSetting.dep,userSetting.clas,userSetting.abroad);
                            ListView();
                        }
                    });

                    // 表示
                    builder.create().show();
                }
            } else {
                Toast.makeText(this, "ネットワークに接続されていません。\n前回取得時のデータを表示します。", Toast.LENGTH_LONG).show();
                userSetting = UserSetting.getInstance(getApplicationContext());
                parse.parseList = userSetting.parseData;
                parse.GetfilterList(userSetting.grade,userSetting.dep,userSetting.clas,userSetting.abroad);
                ListView();

            }
        } else {
            userSetting = UserSetting.getInstance(getApplicationContext());
            parse.parseList = userSetting.parseData;
            parse.GetfilterList(userSetting.grade,userSetting.dep,userSetting.clas,userSetting.abroad);
            ListView();
            Log.d("a", "reloaded");
        }
    }
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void ListView(){
        List<CustomListView> objects = new ArrayList<>();
        for (int i = 0; i < parse.filterList.size(); i++) {
            Map<String,String> map = parse.filterList.get(i);
            CustomListView clv = new CustomListView();
            clv.setDate(map.get("date"));
            clv.setTerm(map.get("term"));
            clv.setType(map.get("type"));
            clv.setCont(map.get("cont"));
            clv.setClas(map.get("clas"));
            objects.add(clv);
        }
        customAdapater = new CustomAdapter(this, 0, objects);

        ListView listView = (ListView)findViewById(R.id.Listview);
        listView.setAdapter(customAdapater);
    }
    public void alert(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("https://08c3209a9291fd0cede87c16e7f19bd6e0c05c1d.googledrive.com/host/0BwTonu4uzP9sflh1MlRTd204XzVvVk1IMFhyclphUjlqQXc1NFJOM1dPazVxbF9EZEVwQUk/");
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public class AsyncHttpRequest extends AsyncTask<String, Void, String> {
        private Activity m_Activity;
        public ProgressDialog m_ProgressDialog;

        public AsyncHttpRequest(Activity activity) {
            this.m_Activity = activity;
        }
        @Override
        protected void onPreExecute() {

            // プログレスダイアログの生成
            this.m_ProgressDialog = new ProgressDialog(this.m_Activity);

            // プログレスダイアログの設定
            this.m_ProgressDialog.setMessage("データ取得中...");  // メッセージをセット

            // プログレスダイアログの表示
            this.m_ProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... url) {
            try {
                Document document = Jsoup.connect(url[0]).get();
                Elements body = document.getElementsByClass("oshirase");
                HTMLparser.infoData = body.toString();
                Document document2 = Jsoup.connect("https://f65fec82d3baf4bbc4d2bab12233737fffd2033c-www.googledrive.com/host/0BwTonu4uzP9sfnBGQVFzYWFwelp1aDFEbXE3cEhQTDY3YWRJM1E0NlFSQnNldTJBUE5fRkU/").get();
                Elements body2 = document2.getElementsByClass("information");
                HTMLparser.makeData = body2.toString();
                newVersion = document2.title();
                Log.d("html",document2.html());
            } catch (Exception e) {
                e.printStackTrace();
            }
            parse.ParseHTML();
            parse.ParseInfo();
            userSetting = UserSetting.getInstance(getApplicationContext());
            userSetting.parseData = parse.parseList;
            userSetting.savaInstance(getApplicationContext());
            parse.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            ListView();
            if (this.m_ProgressDialog != null && this.m_ProgressDialog.isShowing()) {
                this.m_ProgressDialog.dismiss();
            }
            if(!version.equals(newVersion)){
                alert("アップデート情報","新しいバージョンがあります。\n今からアプデしますか？");
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.alert_dialog, (ViewGroup) findViewById(R.id.alertdialog_layout));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("所属の設定");
            builder.setView(layout);
            final Spinner spgrade = (Spinner) layout.findViewById(R.id.spgrade);
            final Spinner spdep = (Spinner) layout.findViewById(R.id.spdep);
            final Spinner spclass = (Spinner) layout.findViewById(R.id.spclass);
            final CheckBox checkabroiad = (CheckBox) layout.findViewById(R.id.abroad);
            builder.setPositiveButton("おーけー", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // おーけー ボタンクリック処理
                    userSetting = UserSetting.getInstance(getApplicationContext());
                    userSetting.grade = (String) spgrade.getSelectedItem();
                    userSetting.dep = (String) spdep.getSelectedItem();
                    userSetting.clas = (String) spclass.getSelectedItem();
                    if (checkabroiad.isChecked()){
                        userSetting.abroad = "留学生";
                    } else {
                        userSetting.abroad = null;
                    }
                    userSetting.savaInstance(getApplicationContext());
                    parse.GetfilterList(userSetting.grade,userSetting.dep,userSetting.clas,userSetting.abroad);
                    ListView();
                }
            });

            // 表示
            builder.create().show();
        } else if(id == R.id.action_update) {
            if (isConnected(this.getApplicationContext())) {
                final String url = "http://www.ibaraki-ct.ac.jp/?page_id=501";
                AsyncHttpRequest task = new AsyncHttpRequest(this);
                task.execute(url);
            } else {
                Toast.makeText(this, "ネットワークに接続されていません。", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
