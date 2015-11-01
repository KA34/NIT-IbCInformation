package com.ka34.nit_ibc_information;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    final String url = "http://www.ibaraki-ct.ac.jp/?page_id=501";
    final String version = "0.3";
    String newVersion;
    ListData data = new ListData();
    private UserSetting userSetting;
    CustomAdapter customAdapter;
    public static AsyncHttpRequest mTask;
    private ProgressDialog mDialog;
    private Context mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // onPreExecute ～ onPostExecute までの判別フラグ
    private static boolean isInProgress;
    private boolean isFirstCreate = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        Log.d("isInProgress", String.valueOf(isInProgress));
        if(!isInProgress) {
            if (ListData.infoData == null) {
                if (isConnected(this.getApplicationContext())) {
                    if (mTask != null) {
                        mTask.cancel(true);
                    }
                    // プログレスダイアログを表示
                    mTask = new AsyncHttpRequest(this);
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
                        final CheckBox checkabroad = (CheckBox) layout.findViewById(R.id.abroad);
                        builder.setPositiveButton("おーけー", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // おーけー ボタンクリック処理
                                userSetting = UserSetting.getInstance(getApplicationContext());
                                userSetting.grade = (String) spgrade.getSelectedItem();
                                userSetting.dep = (String) spdep.getSelectedItem();
                                userSetting.clas = (String) spclass.getSelectedItem();
                                if (checkabroad.isChecked()) {
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
                        isFirstCreate = true;
                        mTask.execute(url);
                    }
                } else {
                    Toast.makeText(this, "ネットワークに接続されていません。\n前回取得時のデータを表示します。", Toast.LENGTH_LONG).show();
                    userSetting = UserSetting.getInstance(getApplicationContext());
                    data.parseList = userSetting.parseData;
                    data.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
                    ListView();
                }
            } else {
                userSetting = UserSetting.getInstance(getApplicationContext());
                data.parseList = userSetting.parseData;
                data.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
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
        Log.d("a", "execute ListView");
        setTitle(userSetting.time);
        List<CustomListView> objects = new ArrayList<>();
        for (int i = 0; i < data.filterList.size(); i++) {
//            Log.d(String.valueOf(i), String.valueOf(data.filterList.get(i)));
            Map<String,String> map = data.filterList.get(i);
            CustomListView clv = new CustomListView();
            clv.setDate(map.get("date"));
            clv.setTerm(map.get("term"));
            clv.setType(map.get("type"));
            clv.setCont(map.get("cont"));
            clv.setClas(map.get("clas"));
            objects.add(clv);
        }
        customAdapter = new CustomAdapter(this, 0, objects);

        ListView listView = (ListView)findViewById(R.id.Listview);
        listView.setAdapter(customAdapter);
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
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isConnected(getApplicationContext())) {
                if (mTask != null) {
                    mTask.cancel(true);
                }
                // プログレスダイアログを表示
                mTask = new AsyncHttpRequest (MainActivity.this);
                mTask.execute(url);
            } else {
                Toast.makeText(getApplicationContext(), "ネットワークに接続されていません。", Toast.LENGTH_LONG).show();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

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
                    data.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
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
        if (mTask != null && mTask.isInProcess() && !isFirstCreate) {
            Log.v("", "showDialog called");
            // プログレスダイアログの再表示
            mTask.showDialog();
        }
        isFirstCreate = false;
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


    public class AsyncHttpRequest extends AsyncTask<String, Void, Integer> implements DialogInterface.OnCancelListener {

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
            mDialog.setOnCancelListener(this);
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
            Display d = getWindowManager().getDefaultDisplay();
            int rotation = d.getRotation();
            switch(rotation) {
                //== 0度 ==//
                case Surface.ROTATION_0:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    Log.d("0", String.valueOf(rotation));
                    break;
                //== 90度 ==//
                case Surface.ROTATION_90:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    Log.d("90", String.valueOf(rotation));
                    break;
                //== 180度 ==//
                case Surface.ROTATION_180:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    Log.d("180", String.valueOf(rotation));
                    break;
                //== 270 ==//
                case Surface.ROTATION_270:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    Log.d("270", String.valueOf(rotation));
                    break;
            }

        }

        @Override
        protected Integer doInBackground(String... url) {
            try {
                Document document = Jsoup.connect(url[0]).get();
                Elements body = document.getElementsByClass("oshirase");
                ListData.infoData = body.toString();
                Document document2 = Jsoup.connect("https://f65fec82d3baf4bbc4d2bab12233737fffd2033c-www.googledrive.com/host/0BwTonu4uzP9sfnBGQVFzYWFwelp1aDFEbXE3cEhQTDY3YWRJM1E0NlFSQnNldTJBUE5fRkU/").get();
                Elements body2 = document2.getElementsByClass("information");
                ListData.makeData = body2.toString();
                newVersion = document2.title();
//                Log.d("html",document2.html());
            } catch (Exception e) {
                e.printStackTrace();
            }
            ParseInfo();
            ParseHTML();
            return 1;
        }

        // キャンセル処理
        public void onCancelled(Integer result) {
            Log.v("", "onCancel() called");
            if (mDialog !=  null && mDialog.isShowing() ) {
                dismissDialog();
            }
            isInProgress = false;
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.v("", "onPostExecute() called");
            userSetting = UserSetting.getInstance(getApplicationContext());
            userSetting.parseData = data.parseList;
            userSetting.time = getTime();
            userSetting.savaInstance(getApplicationContext());
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            data.GetfilterList(userSetting.grade, userSetting.dep, userSetting.clas, userSetting.abroad);
            ListView();

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

        @Override
        public void onCancel(DialogInterface dialog) {
            Log.v("Dialog", "CANCEL");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            isInProgress = false;
            cancel(true);
        }

        private Map<String,String> tmpMap = new HashMap<>();
        public String str = "";

        private void ParseHTML(){
            Document doc = Jsoup.parse(ListData.infoData);
            Elements ele = doc.getElementsByTag("tr");
            ListData.infoData = ele.toString();
            String[] trList = ListData.infoData.split("</tr>", 0);
            for (int i=0; i<trList.length; i++) {
                if (isCancelled()) { break; }
                String[] tdList = trList[i].split("</td>",0);
                String regex = "(\\d{4}年\\d+月\\d+日\\（\\p{InCJKUnifiedIdeographs}\\）)";
                tdList[0] = extractMatchString(regex, tdList[0]);
                String[] pList = tdList[2].split("</p>", 0);
                if(Integer.valueOf(getDate()) > Integer.valueOf(getCompare(tdList[0],tdList[0]))){
                    break;
                }
                Log.d(String.valueOf(i),getCompare(tdList[0],tdList[0]));
//                Log.d("td "+String.valueOf(i),tdList[0]);//更新日
                for (int j=1; j<pList.length-1; j++) {      //length-1
                    pList[j] = pList[j].substring(4);
                    String[] spList = pList[j].split("<span style=\"text-decoration: underline;\">");
                    ArrayList<String> spArrayList = new ArrayList<>();
                    Pattern p = Pattern.compile("^<br>");
                    for (int k = 1; k <spList.length ; k++) {
                        Matcher m = p.matcher(spList[k]);
                        if (m.find()) {spList[k] = spList[k].substring(4);}
                        Pattern ps = Pattern.compile("(^</span>$)|(^</span><br>$)");
                        Matcher ms = ps.matcher(spList[k]);
                        Pattern ps2 = Pattern.compile("^</span>[●◎☆]");
                        Matcher ms2 = ps2.matcher(spList[k]);
                        if (!ms.find()) {
                            if(ms2.find()) {
                                spList[k] = spList[k].substring(7);
                                spArrayList.set(spArrayList.size()-1,spList[k-1] + spList[k]);
                            } else {
                                spArrayList.add(spList[k]);
                            }
                        }
                    }
                    String[] temp = spArrayList.toArray(new String[spArrayList.size()]);
                    spList = temp;

                    for (int k = 0; k <spList.length ; k++) {
                        ArrayList<String> tmpList = new ArrayList<>();
                        String[] brList = spList[k].split("<br>");
                        Pattern pbr = Pattern.compile("(^</span>)|(</span>$)");
                        Pattern pbr2 = Pattern.compile("</span>[●◎☆]");
                        Pattern blankbr = Pattern.compile("^　+$");
                        for (int l = 0; l <brList.length ; l++) {
                            Matcher mbr = pbr.matcher(brList[l]);
                            brList[l] = mbr.replaceFirst("");
                            Matcher mbr2 = pbr2.matcher(brList[l]);
                            if(mbr2.find()) {
                                String[] tmp = brList[l].split("</span>");
                                spArrayList = new ArrayList<>();
                                for (int m = 0; m <brList.length ; m++) {
                                    if(m==l){
                                        spArrayList.add(tmp[0]);
                                        spArrayList.add(tmp[1]);
                                    } else {
                                        spArrayList.add(brList[m]);
                                    }
                                }
                                temp = spArrayList.toArray(new String[spArrayList.size()]);
                                brList = temp;
                            }
                            str = str+"\n"+brList[l];

                            Matcher mblankbr = blankbr.matcher(brList[l]);
                            if (!mblankbr.find()){
                                tmpList.add(brList[l]);
//                                Log.d("brList" + String.valueOf(l) + " ", brList[l]);
                            }
                        }
                        temp = tmpList.toArray(new String[tmpList.size()]);
                        brList = temp;

                        String other = null;
                        for (int n = 1; n<brList.length; n++) {
                            String type;
                            tmpMap = new HashMap<>();
                            if (brList[n].indexOf("☆")==0) {
                                type = "change";
                            }else if(brList[n].indexOf("●")==0) {
                                type = "cancel";
                            }else if(brList[n].indexOf("◎")==0){
                                type = "makeup";
                            } else {
                                type = "?";
                            }
                            if(extractMatchString("^[●◎☆]\\S+\\s+(\\d|\\d([・，,－]\\d)*)限\\s",brList[n])!=null) {
                                String clas,term,cont;
                                clas = extractMatchString("^[●◎☆](\\S+)",brList[n]);
                                term = extractMatchString("\\s(\\d|\\d([・，,]\\d)*限)\\s",brList[n]);
                                cont = extractMatchString("限\\s+(.+)$",brList[n]);
                                Pattern pnbsp = Pattern.compile("&nbsp;");
                                Matcher mnbsp;
                                if (cont != null) {
                                    mnbsp = pnbsp.matcher(cont);
                                    cont = mnbsp.replaceAll("");
                                }
                                if (clas != null) {
                                    String compare = getCompare(brList[0],tdList[0]);
                                    if(clas.matches("^[１２]－[１２３４５]")) {
                                        Pattern pbar = Pattern.compile("－");
                                        Matcher mbar = pbar.matcher(clas);
                                        clas = mbar.replaceAll("の");
                                    }
                                    if(clas.matches("^[１２３４５][ＭＳＥＤＣ]")||clas.matches("^[１２３４５]年$")||clas.matches("^[１２]の[１２３４５]")||clas.matches("^[１２３４５]年留学生")) {
                                        if (!DeleteSearch(compare,clas,term)) {
                                            AddparseList(brList[0], tdList[0], type, clas, term, cont, compare);
                                        }
/*                                } else {
                                    Log.d(brList[0], clas); 所属例外 */
                                    }
                                }

                            }else{
                                other = other +"\n"+ brList[n];
                            }
                        }
                        if (other!=null){
                            other = other.substring(5);
                            tmpMap.put("date", brList[0]);
                            tmpMap.put("update", tdList[0]);
                            tmpMap.put("type", "other");
                            tmpMap.put("cont",other);
                            data.parseList.add(tmpMap);
                        }
                    }
                }
            }
        }

        public void ParseInfo(){
            data.parseList = new ArrayList<>();
            String[] brList = ListData.makeData.split("</p>", 0);
            for (int i = 1; i < brList.length-1; i++) {
                brList[i] = brList[i].substring(6);
                String[] dataList = brList[i].split(",",0);
                if (Integer.valueOf(getDate()) < Integer.valueOf(dataList[1])) {
                    tmpMap = new HashMap<>();
                    switch (dataList[0]) {
                        case "delete":
                            tmpMap.put("compare", dataList[1]);
                            tmpMap.put("clas", dataList[2]);
                            tmpMap.put("term", dataList[3]);
                            data.deleteList.add(tmpMap);
                            break;
                        case "remake":
                            tmpMap.put("compare", dataList[1]);
                            tmpMap.put("clas", dataList[4]);
                            tmpMap.put("term", dataList[5]);
                            data.deleteList.add(tmpMap);
                            tmpMap = new HashMap<>();
                            tmpMap.put("compare", dataList[1]);
                            tmpMap.put("date", dataList[2]);
                            tmpMap.put("type", dataList[3]);
                            tmpMap.put("clas", dataList[4]);
                            tmpMap.put("term", dataList[5]);
                            tmpMap.put("cont", dataList[6]);
                            data.parseList.add(tmpMap);
                            break;
                        case "new":
                            if (dataList[3].equals("other")) {
                                tmpMap.put("compare", dataList[1]);
                                tmpMap.put("date", dataList[2]);
                                tmpMap.put("type", dataList[3]);
                                tmpMap.put("cont", dataList[4]);
                                data.parseList.add(tmpMap);
                                break;
                            } else {
                                tmpMap.put("compare", dataList[1]);
                                tmpMap.put("date", dataList[2]);
                                tmpMap.put("type", dataList[3]);
                                tmpMap.put("clas", dataList[4]);
                                tmpMap.put("term", dataList[5]);
                                tmpMap.put("cont", dataList[6]);
                                data.parseList.add(tmpMap);
                                break;
                            }
                    }
                }
            }
        }
        public String extractMatchString(String regex, String target) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(target);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return null;
            }
        }
        private void AddparseList(String date, String update, String type, String clas, String term, String cont, String compare){
            tmpMap = new HashMap<>();
            tmpMap.put("date", date);
//            tmpMap.put("update", update);
            tmpMap.put("type", type);
            tmpMap.put("clas", clas);
            tmpMap.put("term", term);
            tmpMap.put("cont", cont);
            tmpMap.put("compare", compare);
            data.parseList.add(tmpMap);
        }

        public String fullWidthNumberToHalfWidthNumber(String str) {
            if (str == null) {
                throw new IllegalArgumentException();
            }
            StringBuilder sb = new StringBuilder(str);
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if ('０' <= c && c <= '９') {
                    sb.setCharAt(i, (char) (c - '０' + '0'));
                }
            }
            return sb.toString();
        }
        public boolean DeleteSearch(String compare, String clas, String term){
            for (int q = 0; q < data.deleteList.size(); q++) {
                Map<String, String> tmpdelMap = data.deleteList.get(q);
                if (tmpdelMap.get("compare").equals(compare) && tmpdelMap.get("clas").equals(clas) && tmpdelMap.get("term").equals(term)) {
                    data.deleteList.remove(q);
                    return true;
                }
            }
            return false;
        }
        public String getCompare(String brlist, String tdlist){
            Pattern pmonth = Pattern.compile("\\d\\d月");
            Matcher mmonth = pmonth.matcher(brlist);
            String month;
            if (mmonth.find()){
                month = extractMatchString("(\\d\\d)月", brlist);
            } else {
                month = "0"+ extractMatchString("(\\d)月", brlist);
            }
            Pattern pd = Pattern.compile("\\d\\d日");
            Matcher md = pd.matcher(brlist);
            String date;
            if (md.find()){
                date = extractMatchString("(\\d\\d)日",brlist);
            } else {
                date = "0"+extractMatchString("(\\d)日",brlist);
            }
            String year = extractMatchString("(\\d\\d\\d\\d)年",tdlist);
            return fullWidthNumberToHalfWidthNumber(year+month+date);
        }
        public String getDate(){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = 1 + cal.get(Calendar.MONTH);// 0 - 11
            if (month < 4){
                return String.valueOf(year-1) + "0331";
            } else {
                return String.valueOf(year) + "0331";
            }
        }
    }
}