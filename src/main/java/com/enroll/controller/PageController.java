package com.enroll.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器
 * 【AI生成】处理前端页面路由
 */
@Controller
public class PageController {

    /**
     * 首页 - 选课管理系统主页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
