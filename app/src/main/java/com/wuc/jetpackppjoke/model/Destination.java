package com.wuc.jetpackppjoke.model;

/**
 * @author: wuchao
 * @date: 2020-01-22 19:06
 * @desciption: 页面路由节点对象
 */
public class Destination {
    /**
     * 页面的pageUrl相当于隐士跳转意图中的host://scheme/path格式
     */
    public String pageUrl;
    /**
     * 页面的id.此处不能重复
     */
    public int id;
    /**
     * 是否需要登录
     */
    public boolean needLogin;
    /**
     * 是否作为首页的第一个展示的页面
     */
    public boolean asStarter;
    /**
     * 标记该页面是fragment 还是activity类型的
     */
    public boolean isFragment;
    /**
     * 全类名 如：com.wuc.jetpackppjoke.ui.home
     */
    public String className;
}
