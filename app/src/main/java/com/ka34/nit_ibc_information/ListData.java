package com.ka34.nit_ibc_information;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ListData {
    public static String infoData;
    public static String makeData;
    public List<Map<String,String>> parseList = new ArrayList<>();
    public List<Map<String,String>> filterList = new ArrayList<>();
    public List<Map<String,String>> deleteList = new ArrayList<>();

    public void GetfilterList(String grade, String dep, String clas,String abroad){
        filterList = new ArrayList<>();
        List<String> filter = new ArrayList<>();
        if (grade!=null){
            filter.add(grade+dep);
            filter.add(grade+"年");
            filter.add("全学年");
            if(clas!=null){
                filter.add(grade+"の"+clas);
            }
            if (abroad!=null){
                filter.add(grade+"年"+abroad);
            }
        }
        for (int i = 0; i < parseList.size(); i++) {
            Map<String, String> tmpMap;
            tmpMap = parseList.get(i);
            if (!tmpMap.get("type").equals("other")) {
                String tmpclas = tmpMap.get("clas");
                for (int j = 0; j < filter.size(); j++) {
                    if (tmpclas.equals(filter.get(j))) {
                        filterList.add(tmpMap);
                    }
                }
            }
        }
        sort("compare");
    }
    public void sort(final String order){
        Collections.sort(filterList, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> map1, Map<String, String> map2) {

                String S1 = map1.get(order);
                String S2 = map2.get(order);

                return S2.compareTo(S1);
            }
        });
    }

/* キャンセル処理のために移動 */
/*    public void ParseHTML(){
        Document doc = Jsoup.parse(infoData);
        Elements ele = doc.getElementsByTag("tr");
        infoData = ele.toString();
        String[] trList = infoData.split("</tr>", 0);
        for (int i=0; i<trList.length; i++) {
            String[] tdList = trList[i].split("</td>",0);
            String regex = "(\\d{4}年\\d+月\\d+日\\（\\p{InCJKUnifiedIdeographs}\\）)";
            tdList[0] = extractMatchString(regex,tdList[0]);
            String[] pList = tdList[2].split("</p>", 0);
//            Log.d("td "+String.valueOf(i),tdList[0]);//更新日
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
                    String[] brList = spList[k].split("<br>");
                    Pattern pbr = Pattern.compile("(^</span>)|(</span>$)");
                    Pattern pbr2 = Pattern.compile("</span>[●◎☆]");
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
                    }
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
                                Pattern pmonth = Pattern.compile("\\d\\d月");
                                Matcher mmonth = pmonth.matcher(brList[0]);
                                String month;
                                if (mmonth.find()){
                                    month = extractMatchString("(\\d\\d)月", brList[0]);
                                } else {
                                    month = "0"+ extractMatchString("(\\d)月", brList[0]);
                                }
                                Pattern pd = Pattern.compile("\\d\\d日");
                                Matcher md = pd.matcher(brList[0]);
                                String date;
                                if (md.find()){
                                    date = extractMatchString("(\\d\\d)日",brList[0]);
                                } else {
                                    date = "0"+extractMatchString("(\\d)日",brList[0]);
                                }
                                String year = extractMatchString("(\\d\\d\\d\\d)年",tdList[0]);
                                String compare = fullWidthNumberToHalfWidthNumber(year+month+date);
                                if(clas.matches("^[１２]－[１２３４５]")) {
                                    Pattern pbar = Pattern.compile("－");
                                    Matcher mbar = pbar.matcher(clas);
                                    clas = mbar.replaceAll("の");
                                }


                                if(clas.matches("^[１２３４５][ＭＳＥＤＣ]")||clas.matches("^[１２３４５]年$")||clas.matches("^[１２]の[１２３４５]")||clas.matches("^[１２３４５]年留学生")) {
                                    if (!DeleteSearch(compare,clas,term)) {
                                        AddparseList(brList[0], tdList[0], type, clas, term, cont, compare);
                                    }
                                } else {
                                    Log.d(brList[0], clas); 所属例外
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
                        parseList.add(tmpMap);
                    }
                }
            }
        }
    }

    public void ParseInfo(){
        parseList = new ArrayList<>();
        String[] brList = makeData.split("</p>", 0);
        for (int i = 1; i < brList.length-1; i++) {
            brList[i] = brList[i].substring(6);
            String[] dataList = brList[i].split(",",0);
            tmpMap = new HashMap<>();
            switch (dataList[0]) {
                case "delete":
                    tmpMap.put("compare", dataList[1]);
                    tmpMap.put("clas", dataList[2]);
                    tmpMap.put("term", dataList[3]);
                    deleteList.add(tmpMap);
                    break;
                case "remake":
                    tmpMap.put("compare", dataList[1]);
                    tmpMap.put("clas", dataList[4]);
                    tmpMap.put("term", dataList[5]);
                    deleteList.add(tmpMap);
                    tmpMap = new HashMap<>();
                    tmpMap.put("compare", dataList[1]);
                    tmpMap.put("date", dataList[2]);
                    tmpMap.put("type", dataList[3]);
                    tmpMap.put("clas", dataList[4]);
                    tmpMap.put("term", dataList[5]);
                    tmpMap.put("cont", dataList[6]);
                    parseList.add(tmpMap);
                    break;
                case "new":
                    if (dataList[3].equals("other")) {
                        tmpMap.put("compare", dataList[1]);
                        tmpMap.put("date", dataList[2]);
                        tmpMap.put("type", dataList[3]);
                        tmpMap.put("cont", dataList[4]);
                        parseList.add(tmpMap);
                        break;
                    } else {
                        tmpMap.put("compare", dataList[1]);
                        tmpMap.put("date", dataList[2]);
                        tmpMap.put("type", dataList[3]);
                        tmpMap.put("clas", dataList[4]);
                        tmpMap.put("term", dataList[5]);
                        tmpMap.put("cont", dataList[6]);
                        parseList.add(tmpMap);
                        break;
                    }
            }
        }
    }

    public static String extractMatchString(String regex, String target) {
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
        tmpMap.put("update", update);
        tmpMap.put("type", type);
        tmpMap.put("clas", clas);
        tmpMap.put("term", term);
        tmpMap.put("cont", cont);
        tmpMap.put("compare", compare);
        parseList.add(tmpMap);
    }
*/
/*    public static String fullWidthNumberToHalfWidthNumber(String str) {
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
        for (int q = 0; q < deleteList.size(); q++) {
            Map<String, String> tmpdelMap = deleteList.get(q);
            if (tmpdelMap.get("compare").equals(compare) && tmpdelMap.get("clas").equals(clas) && tmpdelMap.get("term").equals(term)) {
                deleteList.remove(q);
                return true;
            }
        }
        return false;
    }
*/
}