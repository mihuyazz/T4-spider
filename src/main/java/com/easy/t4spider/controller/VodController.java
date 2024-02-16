package com.easy.t4spider.controller;

import com.easy.t4spider.crawler.Spider;
import com.easy.t4spider.spider.CeChi;
import com.easy.t4spider.spider.Duanju;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/vod")
public class VodController {

    private List<String> site_list = Arrays.asList("Duanju", "Cechi");
    private LinkedHashMap<String, Spider> siteMap = new LinkedHashMap<>() {{
        put("Duanju", new Duanju());
        put("Cechi", new CeChi());
    }};

    @GetMapping
    public String get(String wd, String ac, String quick, String play, String flag,
                      String filter, String t, String pg, String ext, String ids, String q,
                      String douban, String sites, String ali_token, String timeout) throws Exception {
        Boolean homeFlag = false;
        // 站点
        if (sites.length() > 0 && !Objects.isNull(siteMap.get(sites))) {
            homeFlag = true;
        } else {
            return "";
        }
        // 分类
        if (StringUtils.isNotBlank(t)) {
            String s = siteMap.get(sites).categoryContent(t, pg, true, null);
            return s;
        }
        // 详情
        if (StringUtils.isNotBlank(ac)) {
            if (ids.split("$").length < 2) {
                return siteMap.get(sites).detailContent(Arrays.asList(ids.split("$")));
            } else {
                // todo
            }
        }
        // 搜索
        if (StringUtils.isNotBlank(wd)) {
            return siteMap.get(sites).searchContent(wd,true);
        }
        // 播放
        if (StringUtils.isNotBlank(play) && StringUtils.isNotBlank(flag)) {
            return siteMap.get(sites).playerContent(flag, play, null);
        }
        // 首页
        if (homeFlag) {
            return siteMap.get(sites).homeContent(true);
        }
        return "";
    }
}
