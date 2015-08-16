package com.ka34.nit_ibc_information;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final String url = "http://www.ibaraki-ct.ac.jp/?page_id=501";
    final String version = "0.2";
    String newVersion;
    HTMLparser parse = new HTMLparser();
    private UserSetting userSetting;
    CustomAdapter customAdapater;
    private static AsyncHttpRequest mTask = null;
    private ProgressDialog mDialog = null;
    private Context mContext = null;
    // onPreExecute ～ onPostExecute までの判別フラグ
    private boolean isInProgress = false;
    private boolean isFirstonCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isInProgress) {
            if (HTMLparser.infoData == null) {
                if (isConnected(this.getApplicationContext())) {
                    if (mTask != null) {
                        mTask.cancel(true);
                    }
                    // プログレスダイアログを表示
                    mTask = new AsyncHttpRequest(MainActivity.this);
                    userSetting = UserSetting.getInstance(getApplicationContext());
                    if (userSetting.grade == null) {
                        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                        final View layout = inflater.inflate(R.layout.alert_dialog, (ViewGroup) findViewById(R.id.alertdialog_layout));
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("所属の設定");
                        builder.setView(layout);
                        builder.setCancelable(false);
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
                                if (checkabroiad.isChecked()) {
                                    userSetting.abroad = "留学生";
                                } else {
                                    userSetting.abroad = null;
                                }
                                userSetting.savaInstance(getApplicationContext());
                                mTask.execute(url);
                            }
                        });

                        // 表示
                        builder.create().show();
                    } else {
                        isFirstonCreate = true;
                        mTask.execute(url);
                    }
                } else {
                    Toast.makeText(this, "ネットワークに接続されていません。\n前回取得時のデータを表示します。", Toast.LENGTH_LONG).show();
                    userSetting = UserSetting.getInstance(getApplicationContext());
                    parse.parseList = userSetting.parseData;
                    parse.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
                    ListView();

                }
            } else {
                userSetting = UserSetting.getInstance(getApplicationContext());
                parse.parseList = userSetting.parseData;
                parse.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
                ListView();
                Log.d("a", "reloaded");

            }
        }
    }
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void ListView(){
        setTitle(userSetting.time);
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
                if (mTask != null) {
                    mTask.cancel(true);
                }
                // プログレスダイアログを表示
                mTask = new AsyncHttpRequest (MainActivity.this);
                mTask.execute(url);
            } else {
                Toast.makeText(this, "ネットワークに接続されていません。", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
    // 存在すれば onCreate, onStart, onResumeの順番で呼ばれる
    @Override
    public void onResume(){
        super.onResume();
        Log.v("", "onResume called");
        // プログレスダイアログの表示開始
        if (mTask != null && mTask.isInProcess() && !isFirstonCreate) {
            Log.v("", "showDialog called");
            // プログレスダイアログの再表示
            mTask.showDialog();
        }
        isFirstonCreate = false;
    }

    // 戻るボタンが押された
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && mTask != null) {
            Log.v("", "keyCode == KeyEvent.KEYCODE_BACK");
            // AsyncTaskのインスタンは使い回しできないので破棄
            mTask.cancel(true);
            mTask = null;
        }
        return super.onKeyDown(keyCode, event);
    }

    //ホームボタン押、他のアプリが起動した時に呼ばれる
    @Override
    public void onUserLeaveHint() {
        if (mTask != null) {
            Log.v("", "dismissDialog called");
            // プログレスダイアログを閉じる（2重表示防止）
            mTask.dismissDialog();
            // 処理は継続するのでインスタンスの破棄はしない
            // mTask = null;
        }
        // トーストを表示する
//        Toast.makeText(this, "Good bye" , Toast.LENGTH_SHORT).show();
    }

    public class AsyncHttpRequest extends AsyncTask<String, Void, Integer> {


        // インスタンス生成
        public AsyncHttpRequest(Context context) {
            mContext = context;
        }
        // プログレスダイアログを閉じる
        public void dismissDialog() {
            if (mDialog !=  null) {
                mDialog.dismiss();
            }
            mDialog = null;
        }

        // Progress Dialog を表示
        public void showDialog() {
            // プログレスダイアログの生成
            mDialog = new ProgressDialog(mContext);
            // プログレスダイアログの設定
            mDialog.setMessage("データ取得中...");  // メッセージをセット
            mDialog.setCancelable(true);
            // ダイアログの外部をタッチしてもダイアログを閉じない
            mDialog.setCanceledOnTouchOutside(false);
            // プログレスダイアログの表示
            mDialog.show();
        }

        public String getTime(){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);// 0 - 11
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);

            return year + "/" + (month+1) + "/" + day + "　" + hour + "時" + minute + "分" + second + "秒更新";
        }

        @Override
        protected void onPreExecute() {
            isInProgress = true;
            showDialog();
        }

        @Override
        protected Integer doInBackground(String... url) {
            try {
                Document document = Jsoup.connect(url[0]).get();
                Elements body = document.getElementsByClass("oshirase");
                HTMLparser.infoData = body.toString();
                Document document2 = Jsoup.connect("https://f65fec82d3baf4bbc4d2bab12233737fffd2033c-www.googledrive.com/host/0BwTonu4uzP9sfnBGQVFzYWFwelp1aDFEbXE3cEhQTDY3YWRJM1E0NlFSQnNldTJBUE5fRkU/").get();
                Elements body2 = document2.getElementsByClass("information");
                HTMLparser.makeData = body2.toString();
                newVersion = document2.title();
//                Log.d("html",document2.html());
            } catch (Exception e) {
                e.printStackTrace();
            }
            parse.ParseInfo();
            parse.ParseHTML();
            userSetting = UserSetting.getInstance(getApplicationContext());
            userSetting.parseData = parse.parseList;
            userSetting.time = getTime();
            userSetting.savaInstance(getApplicationContext());
            parse.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
            // キャンセルが押された場合
            if (isCancelled() ) {
                return 0;
            }
            return 1;
        }

        // キャンセル処理
        public void onCancelled(Integer result) {
            Log.v("", "onCancel() called");
            if (mDialog !=  null && mDialog.isShowing() ) {
                dismissDialog();
            }
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            ListView();
            Log.v("", "onPostExecute() called");
            // ProgressDialog の削除
            Log.d("", String.valueOf(mDialog));
            if (mDialog !=  null) {
                dismissDialog();
            }
            Toast.makeText(mContext, "更新成功!" , Toast.LENGTH_SHORT).show();
            // AsyncTaskの終了
            isInProgress = false;
            if(!version.equals(newVersion)){
                alert("アップデート情報","新しいバージョンがあります。\nアップデートしてください");
            }
        }
        public synchronized boolean isInProcess() { return isInProgress; }

    }
}
