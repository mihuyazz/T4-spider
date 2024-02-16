package com.easy.t4spider.spider;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easy.t4spider.bean.Class;
import com.easy.t4spider.bean.Filter;
import com.easy.t4spider.bean.Result;
import com.easy.t4spider.bean.Vod;
import com.easy.t4spider.crawler.Spider;
import com.easy.t4spider.net.OkHttp;
import com.easy.t4spider.utils.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CeChi extends Spider {
    private static String siteUrl = "https://www.algdts.com";

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", Util.CHROME);
        header.put(":authority:", "www.algdts.com");
        header.put(":method:", "GET");
        header.put(":path:", "/");
        header.put(":scheme:", "https");
        header.put("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
        header.put("Sec-Ch-Ua-Mobile", "?0");
        header.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        header.put("Sec-Fetch-Dest", "document");
        header.put("Sec-Fetch-Mode", "navigate");
        header.put("Sec-Fetch-Site", "none");
        header.put("Sec-Fetch-User", "?1");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        header.put("Accept-Encoding","gzip, deflate, br");
        header.put("Accept-Language","zh-CN,zh;q=0.9");
        header.put("Cache-Control","max-age=0");
        header.put("Cookie", "zanpiancms_search_wd=%7Bvideo%3A%5B%7B%22name%22%3A%22%u7E41%u82B1%22%2C%22url%22%3A%22/search/%25E7%25B9%2581%25E8%258A%25B1.html%22%7D%5D%7D; zanpian_playlog=%7B%22id_9294%22%3A%7B%22log_vid%22%3A9294%2C%22log_sid%22%3A2%2C%22log_pid%22%3A16%2C%22log_urlname%22%3A%22%5Cu7b2c16%5Cu96c6%22%2C%22log_maxnum%22%3A16%2C%22log_addtime%22%3A1705336753%7D%2C%22id_9812%22%3A%7B%22log_vid%22%3A9812%2C%22log_sid%22%3A1%2C%22log_pid%22%3A1%2C%22log_urlname%22%3A%22HD%5Cu4e2d%5Cu5b57%22%2C%22log_maxnum%22%3A1%2C%22log_addtime%22%3A1705330042%7D%7D; PHPSESSID=89b37b1cb688103db63dabc5854d8759; Hm_lvt_ba06987dabe18f8f7e89aad04d7a5694=1705294907,1705316556,1705500408; Hm_lpvt_ba06987dabe18f8f7e89aad04d7a5694=1705502827");
        header.put("Upgrade-Insecure-Requests", "1");
        return header;
    }

    @Override
    public void init(String extend) throws Exception {
        super.init(extend);
        if (!extend.isEmpty()) {
            siteUrl = extend;
        }
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Class> classes = new ArrayList<>();
        List<String> typeIds = Arrays.asList("1", "2", "3", "4", "5");
        List<String> typeNames = Arrays.asList("电影", "电视剧", "动漫", "综艺", "微电影");
        for (int i = 0; i < typeIds.size(); i++) {
            classes.add(new Class(typeIds.get(i), typeNames.get(i)));
        }
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeader()));
        List<Vod> list = new ArrayList<>();
        for (Element li : doc.select("div.swiper-wrapper").eq(0).select("li")) {
            String vid = siteUrl + li.select(".pic-img").attr("href");
            String name = li.select(".pic-img").attr("title");
            String pic = li.select(".pic-img img").attr("data-original");
            String remark = li.select(".sname").text();
            list.add(new Vod(vid, name, pic, remark));
        }
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap();
        for (int i = 0; i < typeIds.size(); i++) {
            String typeId = typeIds.get(i);
            String filterUrl = siteUrl + String.format("/index.php/home/vod/type-id-%s", typeId);
            Document filterDoc = Jsoup.parse(OkHttp.string(filterUrl, getHeader()));
            List<Filter> filterTemp = new ArrayList<>();
            for (Element ul : filterDoc.select("div.top-type div.container").eq(0).select("ul")) {
                String key = ul.id();
                if (key == "") {
                    key = "id";
                }
                String name = ul.select("li").eq(0).text();
                List<Filter.Value> filterValues = new ArrayList<>();
                for (Element li : ul.select("li")) {
                    String n = li.select("a").text();
                    String v = li.select("a").attr("data");
                    if (n.equals(name)) {
                        continue;
                    }
                    filterValues.add(new Filter.Value(n, v));
                }
                filterTemp.add(new Filter(key, name, filterValues));
            }
            filters.put(typeIds.get(i), filterTemp);
        }
        return Result.string(classes, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        HashMap<String, String> ext = new HashMap<>();
        if (extend != null && extend.size() > 0) {
            ext.putAll(extend);
        }
        String cateId = ext.get("id") == null ? "type-id-" + tid : "type-" + ext.get("id");
        String mcid = ext.get("mcid") == null ? "" : "-" + ext.get("mcid");
        String area = ext.get("area") == null ? "" : "-" + ext.get("area");
        String year = ext.get("year") == null ? "" : "-" + ext.get("year");
        String letter = ext.get("letter") == null ? "" : "-" + ext.get("letter");
        String order = "-order-";
        String picm = "-picm-1";
        String pgStr = "-p-" + pg;
        String cateUrl = siteUrl + String.format("/index.php/home/vod/%s%s%s%s%s%s%s%s", cateId, mcid, area, year, letter, order, picm, pgStr);
        Document doc = Jsoup.parse(OkHttp.string(cateUrl, getHeader()));
        List<Vod> list = new ArrayList<>();
        for (Element li : doc.select("div.layout-box ul.pic-list").eq(0).select("li")) {
            String vid = siteUrl + li.select("a").attr("href");
            String name = li.select("a").attr("title");
            String pic = li.select("img").attr("data-original");
            String remark = li.select(".sname").text();
            list.add(new Vod(vid, name, pic, remark));
        }
        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(ids.get(0), getHeader()));
        StringBuilder vod_play_url = new StringBuilder();
        StringBuilder vod_play_from = new StringBuilder();
        Elements titles = doc.select("div.layout-box").select("div.play-title").select("ul").eq(0).select("li");
        Elements lists = doc.select("div.layout-box").select("div.play-list").select("ul");
        for (int i = 0; i < titles.size(); i++) {
            Element li = titles.get(i);
            vod_play_from.append(li.select("a").text());
            if (i != titles.size() -1) {
                vod_play_from.append("$$$");
            }
        }
        for (int i = 0; i < lists.size(); i++) {
            Element ul = lists.get(i);
            Elements liList = ul.select("li");
            for (int j = 0; j < liList.size(); j++) {
                Element li = liList.get(j);
                String name = li.select("a").text();
                String url = siteUrl + li.select("a").attr("href");
                if (j != liList.size() - 1) {
                    vod_play_url.append(name).append("$").append(url).append("#");
                } else {
                    vod_play_url.append(name).append("$").append(url);
                }
            }
            if (i != lists.size() -1) {
                vod_play_url.append("$$$");
            }
        }
        String title = doc.select("h1.text-overflow").text();
        Elements videoInfo = doc.select("video-box").select("video-info").eq(1);

        String remark = "";
        String typeName = "";
        String actor = "";
        String director = "";
        String year = "";
        for (Element div :videoInfo.select("div")) {
            String span = div.select("span").text();
            String text = div.text();
            if (span.contains("状态")) {
                remark = text;
            }
            if (span.contains("类型")) {
                typeName = text;
            }
            if (span.contains("主演")) {
                actor = text;
            }
            if (span.contains("导演")) {
                director = text;
            }
            if (span.contains("年代")) {
                year = text;
            }
        }
        String brief = doc.select("div.article-content").select("p").text();
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodYear(year);
        vod.setVodName(title);
        vod.setVodActor(actor);
        vod.setVodRemarks(remark);
        vod.setVodContent(brief);
        vod.setVodDirector(director);
        vod.setTypeName(typeName);
        vod.setVodPlayFrom(vod_play_from.toString());
        vod.setVodPlayUrl(vod_play_url.toString());
        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String searchUrl = siteUrl + "/search/" + URLEncoder.encode(key) + ".html";
        Document doc = Jsoup.parse(OkHttp.string(searchUrl, getHeader()));
        List<Vod> list = new ArrayList<>();
        for (Element li : doc.select("#content").select("li")) {
            String vid = siteUrl + li.select("div").eq(0).select("a").attr("href");
            String name = li.select("div").eq(1).select("a").attr("title");
            String pic = li.select("div").eq(0).select("a img").attr("data-original");
            String remark = li.select("div").eq(1).select("p").eq(2).text();
            list.add(new Vod(vid, name, pic, remark));
        }
        return Result.string(list);
    }


    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String content = OkHttp.string(id, getHeader());
        Matcher matcher = Pattern.compile("zanpiancms_player = (.*?);</script>").matcher(content);
        String json = matcher.find() ? matcher.group(1) : "";
        JSONObject parse = JSON.parseObject(json);
        String realUrl = parse.getString("url");
        return Result.get().url(realUrl).header(getHeader()).string();
    }
}
