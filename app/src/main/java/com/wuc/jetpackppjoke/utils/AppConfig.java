package com.wuc.jetpackppjoke.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wuc.jetpackppjoke.model.BottomBar;
import com.wuc.jetpackppjoke.model.Destination;
import com.wuc.jetpackppjoke.model.SofaTab;
import com.wuc.libcommon.global.AppGlobals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author: wuchao
 * @date: 2020-01-22 19:15
 * @desciption: 通用配置类
 */
public class AppConfig {
    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottomBar;
    private static SofaTab sSofaTab, sFindTabConfig;

    /**
     * 获取页面路由节点对象
     */
    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null) {
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            });
        }
        return sDestConfig;
    }

    private static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getAssets();
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder builder = new StringBuilder();
        try {
            is = assets.open(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {

            }
        }
        return builder.toString();
    }

    /**
     * 主页面底部导航栏可配置化对象
     */
    public static BottomBar getBottomBarConfig() {
        if (sBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }

    public static SofaTab getSofaTabConfig() {
        if (sSofaTab == null) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(sSofaTab.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return Integer.compare(o1.index, o2.index);
                    //return o1.index < o2.index ? -1 : 1;
                }
            });
        }
        return sSofaTab;
    }

}
