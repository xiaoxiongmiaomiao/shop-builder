#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
shop-builder 全栈项目脚手架
============================
按《一键店铺小程序开发手册》生成三类初始骨架：
  - 后端 ai-shop-system（Spring Boot 4.1 + MyBatis-Plus）
  - 微信小程序（Taro 或 Weapp）
  - 管理后台（Vue3 + Vben 5.0）
加载 shop-builder 后由 WorkBuddy 调用，或由开发者手动执行。

用法:
  python scaffold_shop_builder.py --target <输出目录> [--mini-framework taro|weapp]
                              [--no-backend] [--no-admin] [--with-readme]

示例:
  python scaffold_shop_builder.py --target ./my-shop --mini-framework taro
"""
import argparse
import os
from pathlib import Path

JAVA_PKG = "com/ai/shop"


def write_file(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(f"  + {path}")


# ----------------------------- 后端骨架 -----------------------------
def gen_backend(root: Path, with_a2m: bool = False):
    base = root / "ai-shop-system"
    (base / "src/main/java").mkdir(parents=True, exist_ok=True)
    (base / "src/main/resources").mkdir(parents=True, exist_ok=True)

    # pom.xml（--with-a2m 时注入支付宝支付依赖）
    write_file(base / "pom.xml", POM_XML.replace("<!--EXTRA_DEPS-->", A2M_DEPENDENCY if with_a2m else ""))

    # AI 按量付费模块（probe → pay → complete → ack / 402 / Payment-Needed / Payment-Proof）
    if with_a2m:
        if A2M_ASSETS.is_dir():
            for f in A2M_ASSETS.glob("*.java"):
                write_file(base / f"src/main/java/{JAVA_PKG}/pay/a2m/{f.name}", f.read_text(encoding="utf-8"))
        else:
            print(f"  ! 未找到 A2M 模板目录: {A2M_ASSETS}，跳过按量付费模块")

    # 启动类
    write_file(
        base / f"src/main/java/{JAVA_PKG}/AiShopApplication.java",
        AI_SHOP_APPLICATION,
    )

    # 全局配置
    write_file(
        base / f"src/main/java/{JAVA_PKG}/config/MybatisPlusConfig.java",
        MYBATIS_PLUS_CONFIG,
    )
    write_file(
        base / f"src/main/java/{JAVA_PKG}/utils/OrderUtils.java",
        ORDER_UTILS,
    )

    # 基础实体
    write_file(
        base / f"src/main/java/{JAVA_PKG}/module/base/entity/BaseEntity.java",
        BASE_ENTITY,
    )

    # 示例：系统用户
    write_file(
        base / f"src/main/java/{JAVA_PKG}/modules/entity/SysUser.java",
        SYS_USER,
    )
    write_file(
        base / f"src/main/java/{JAVA_PKG}/modules/mapper/SysUserMapper.java",
        SYS_USER_MAPPER,
    )
    write_file(
        base / f"src/main/java/{JAVA_PKG}/modules/service/SysUserService.java",
        SYS_USER_SERVICE,
    )
    write_file(
        base / f"src/main/java/{JAVA_PKG}/modules/service/impl/SysUserServiceImpl.java",
        SYS_USER_SERVICE_IMPL,
    )
    write_file(
        base / f"src/main/java/{JAVA_PKG}/controller/SysUserController.java",
        SYS_USER_CONTROLLER,
    )

    # 资源
    write_file(base / "src/main/resources/application.yml", APP_YML)
    write_file(base / "src/main/resources/application-dev.yml", APP_DEV_YML)

    write_file(base / "README.md", BACKEND_README)


# ----------------------------- 小程序骨架 -----------------------------
def gen_miniprogram(root: Path, framework: str):
    mp = root / "shop-mini"
    if framework == "taro":
        write_file(mp / "package.json", MP_TARO_PACKAGE_JSON)
        write_file(mp / "src/app.config.ts", MP_TARO_APP_CONFIG)
        write_file(mp / "src/utils/requestUtils.ts", REQUEST_UTILS_TS)
        for p in ("home", "category", "goods-detail", "cart", "profile"):
            write_file(mp / f"src/pages/{p}/index.tsx", MP_PAGE_TSX.replace("{{PAGE}}", p))
        write_file(mp / "README.md", MP_TARO_README)
    else:
        write_file(mp / "app.json", MP_WEAPP_APP_JSON)
        write_file(mp / "utils/requestUtils.js", REQUEST_UTILS_JS)
        for p in ("home", "category", "goods-detail", "cart", "profile"):
            write_file(mp / f"pages/{p}/index.js", MP_PAGE_JS.replace("{{PAGE}}", p))
            write_file(mp / f"pages/{p}/index.wxml", "<view>{{PAGE}}</view>".replace("{{PAGE}}", p))
            write_file(mp / f"pages/{p}/index.wxss", "/* {{PAGE}} */".replace("{{PAGE}}", p))
            write_file(mp / f"pages/{p}/index.json", "{}")
        write_file(mp / "README.md", MP_WEAPP_README)


# ----------------------------- 管理后台骨架 -----------------------------
def gen_admin(root: Path):
    admin = root / "shop-admin"
    write_file(admin / "package.json", ADMIN_PACKAGE_JSON)
    for mod in ("system", "goods", "order", "user"):
        write_file(admin / f"src/views/{mod}/index.vue", ADMIN_VIEW_VUE.replace("{{MOD}}", mod))
        write_file(admin / f"src/api/{mod}.ts", ADMIN_API_TS.replace("{{MOD}}", mod))
    write_file(admin / "README.md", ADMIN_README)


# ----------------------------- 模板内容 -----------------------------
POM_XML = """<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
  </parent>
  <groupId>com.ai</groupId>
  <artifactId>ai-shop-system</artifactId>
  <version>1.0.0</version>
  <properties>
    <java.version>17</java.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId><version>3.5.7</version></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
    <!--EXTRA_DEPS-->
  </dependencies>
</project>
"""

# ---- AI 按量付费（Machine Pay / 402）附加依赖 ----
A2M_DEPENDENCY = """    <!-- 支付宝 AI 按量付费（402 / Payment-Needed / Payment-Proof） -->
    <dependency><groupId>com.alipay.sdk</groupId><artifactId>alipay-sdk-java</artifactId><version>4.40.0.ALL</version></dependency>
    <dependency><groupId>com.alibaba</groupId><artifactId>fastjson</artifactId><version>1.2.83</version></dependency>
"""

# skill 根目录（scripts/ 的上一级），用于定位 assets/a2m 模板
SKILL_DIR = Path(__file__).resolve().parent.parent
A2M_ASSETS = SKILL_DIR / "assets" / "a2m"

AI_SHOP_APPLICATION = """package com.ai.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ai.shop")
public class AiShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiShopApplication.class, args);
    }
}
"""

MYBATIS_PLUS_CONFIG = """package com.ai.shop.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
"""

ORDER_UTILS = """package com.ai.shop.utils;

import java.util.Random;

/** 单号生成工具：前缀 + MMDD + 5位随机数 + 校验码(随机数%10000)。例：order20240101001 */
public class OrderUtils {
    private static final Random RANDOM = new Random();

    public static String getOrderNo(String prefix) {
        java.time.LocalDate now = java.time.LocalDate.now();
        String datePart = String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
        int rand = RANDOM.nextInt(100000);
        int check = rand % 10000;
        return prefix + datePart + String.format("%05d", rand) + check;
    }
}
"""

BASE_ENTITY = """package com.ai.shop.module.base.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createUserName;
    private Long createUserId;
    private String updateUserName;
    private Long updateUserId;
    @TableLogic
    private Integer deleteFlag;
    @Version
    private Integer version;
}
"""

SYS_USER = """package com.ai.shop.modules.entity;

import com.ai.shop.module.base.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_users")
public class SysUser extends BaseEntity {
    /** 账号 */
    private String userAccount;
    /** 密码(加密后) */
    private String password;
    /** 用户昵称 */
    private String nickName;
    /** 头像 */
    private String avatar;
    /** 用户状态 0-禁用 1-正常 2-锁定 */
    private Integer status;
    /** 用户类型 0-管理端 1-小程序端 */
    private Integer userType;
    /** 微信 union_id / open_id */
    private String unionId;
    private String openId;
}
"""

SYS_USER_MAPPER = """package com.ai.shop.modules.mapper;

import com.ai.shop.modules.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
"""

SYS_USER_SERVICE = """package com.ai.shop.modules.service;

import com.ai.shop.modules.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysUserService extends IService<SysUser> {
}
"""

SYS_USER_SERVICE_IMPL = """package com.ai.shop.modules.service.impl;

import com.ai.shop.modules.entity.SysUser;
import com.ai.shop.modules.mapper.SysUserMapper;
import com.ai.shop.modules.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
"""

SYS_USER_CONTROLLER = """package com.ai.shop.controller;

import com.ai.shop.modules.entity.SysUser;
import com.ai.shop.modules.service.SysUserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/private/user")
public class SysUserController {
    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public List<SysUser> page() {
        return userService.list();
    }
}
"""

APP_YML = """spring:
  profiles:
    active: dev
server:
  port: 9002
"""

APP_DEV_YML = """spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai-shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      password: root
# 七牛云（文件上传）：在 application 中配置 accessKey/secretKey/bucket/domain
"""

BACKEND_README = """# ai-shop-system 后端

Spring Boot 4.1 + JDK 17 + MyBatis-Plus + MySQL 8 + Redis 7。

## 启动
1. 修改 `src/main/resources/application-dev.yml` 的数据库/Redis 连接。
2. `mvn spring-boot:run`（默认端口 9002）。

## 约定
- 包结构：`com.ai.shop`（config / exception / utils / modules / module）。
- 统一返回：`Response<DataResult<T>>` / `Response<PageResult<T>>`。
- 单号：`OrderUtils.getOrderNo("前缀")`。
- 价格：`decimal(20,2)`；批量操作必须批处理，禁止 for/while 循环。
- 所有实体继承 `BaseEntity`。
- 接口完成后整理 SQL 文件。
"""

MP_TARO_PACKAGE_JSON = """{
  "name": "shop-mini",
  "version": "1.0.0",
  "description": "一键店铺小程序 (Taro)",
  "template": "default",
  "scripts": {
    "dev": "taro build --type weapp --watch",
    "build": "taro build --type weapp"
  },
  "dependencies": {
    "@tarojs/components": "3.x",
    "@tarojs/taro": "3.x",
    "taro-ui": "3.x",
    "react": "18.x"
  }
}
"""

MP_TARO_APP_CONFIG = """export default defineAppConfig({
  pages: [
    'pages/home/index',
    'pages/category/index',
    'pages/goods-detail/index',
    'pages/cart/index',
    'pages/profile/index'
  ],
  window: {
    navigationBarTitleText: '一键店铺'
  },
  tabBar: {
    list: [
      { pagePath: 'pages/home/index', text: '首页' },
      { pagePath: 'pages/category/index', text: '分类' },
      { pagePath: 'pages/cart/index', text: '购物车' },
      { pagePath: 'pages/profile/index', text: '个人中心' }
    ]
  }
})
"""

REQUEST_UTILS_TS = """const BASE_URL = 'http://localhost:9001';

function authHeader(): Record<string, string> {
  const token = (typeof localStorage !== 'undefined' && localStorage.getItem('token')) || '';
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function requestUtils<T = any>(method: string, url: string, data?: any): Promise<T> {
  return new Promise((resolve, reject) => {
    // @ts-ignore
    Taro.request({ url: BASE_URL + url, method: method.toUpperCase(), data, header: authHeader() })
      .then((res: any) => resolve(res.data))
      .catch(reject);
  });
}

export const get = <T = any>(url: string) => requestUtils<T>('get', url);
export const post = <T = any>(url: string, data?: any) => requestUtils<T>('post', url, data);
export const put = <T = any>(url: string, data?: any) => requestUtils<T>('put', url, data);
export const del = <T = any>(url: string) => requestUtils<T>('delete', url);
export const download = (url: string) => requestUtils('get', url);
export const upload = (url: string, filePath: string) =>
  new Promise((resolve, reject) => {
    // @ts-ignore
    Taro.uploadFile({ url: BASE_URL + url, filePath, name: 'file' })
      .then((r: any) => resolve(r.data)).catch(reject);
  });
"""

MP_PAGE_TSX = """export default function {{PAGE}}() {
  return <view>商品页：{{PAGE}}</view>;
}
"""

MP_TARO_README = """# 小程序（Taro）

TypeScript + Taro UI。网络请求统一走 `src/utils/requestUtils.ts`（含 get/post/put/delete/download/upload）。

## 启动
- `npm install && npm run dev`，用微信开发者工具打开 `dist` 目录。
- TabBar 四个菜单：首页 / 分类 / 购物车 / 个人中心（按主题色生成图标）。

## 约定
- 页面跳转用 `Taro.navigateTo` / `redirectTo` / `switchTab`。
- 首页/分类/详情/购物车/个人中心/地址/订单/开票按手册实现。
"""

MP_WEAPP_APP_JSON = """{
  "pages": [
    "pages/home/index",
    "pages/category/index",
    "pages/goods-detail/index",
    "pages/cart/index",
    "pages/profile/index"
  ],
  "window": { "navigationBarTitleText": "一键店铺" },
  "tabBar": {
    "list": [
      { "pagePath": "pages/home/index", "text": "首页" },
      { "pagePath": "pages/category/index", "text": "分类" },
      { "pagePath": "pages/cart/index", "text": "购物车" },
      { "pagePath": "pages/profile/index", "text": "个人中心" }
    ]
  }
}
"""

REQUEST_UTILS_JS = """const BASE_URL = 'http://localhost:9001';

function authHeader() {
  const token = wx.getStorageSync('token') || '';
  return token ? { Authorization: 'Bearer ' + token } : {};
}

function requestUtils(method, url, data) {
  return new Promise((resolve, reject) => {
    wx.request({ url: BASE_URL + url, method: method.toUpperCase(), data, header: authHeader(),
      success: (res) => resolve(res.data), fail: reject });
  });
}

module.exports = {
  get: (u) => requestUtils('get', u),
  post: (u, d) => requestUtils('post', u, d),
  put: (u, d) => requestUtils('put', u, d),
  del: (u) => requestUtils('delete', u),
  download: (u) => requestUtils('get', u),
  upload: (u, filePath) => new Promise((resolve, reject) => {
    wx.uploadFile({ url: BASE_URL + u, filePath, name: 'file', success: (r) => resolve(r.data), fail: reject });
  })
};
"""

MP_PAGE_JS = """Page({ data: { page: '{{PAGE}}' } });
"""

MP_WEAPP_README = """# 小程序（Weapp / 原生）

JavaScript。网络请求统一走 `utils/requestUtils.js`（含 get/post/put/delete/download/upload）。

## 启动
- 微信开发者工具导入本目录，AppID 填你的小程序账号。
"""

ADMIN_PACKAGE_JSON = """{
  "name": "shop-admin",
  "version": "1.0.0",
  "description": "一键店铺管理后台 (Vue3 + Vben 5.0)",
  "scripts": {
    "dev": "pnpm dev",
    "build": "pnpm build"
  },
  "dependencies": {
    "vue": "3.x",
    "axios": "1.x",
    "@vben/...": "5.0.0"
  }
}
"""

ADMIN_VIEW_VUE = """<template>
  <div class="p-4">{{ title }}</div>
</template>

<script setup lang="ts">
// 模块：{{MOD}}
const title = '{{MOD}} 管理';
</script>
"""

ADMIN_API_TS = """import request from '@/utils/http';

// {{MOD}} 模块接口
export function list{{MOD}}(params: any) {
  return request.get('/private/{{MOD}}/page', { params });
}
"""

ADMIN_README = """# 管理后台（Vue3 + Vben 5.0）

## 模块
- `src/views/system` 系统（用户/角色/菜单/字典）
- `src/views/goods` 商品
- `src/views/order` 订单
- `src/views/user` 用户

## 约定
- 分页查询独立 `index.vue`；增删改用分离式 dialog。
- 接口走 Axios，实体/参数严格对齐后端。
- 主分析页前端路由，其余后端路由。
"""

# ----------------------------- 入口 -----------------------------
def main():
    parser = argparse.ArgumentParser(description="shop-builder 全栈项目脚手架")
    parser.add_argument("--target", required=True, help="输出根目录")
    parser.add_argument("--mini-framework", choices=["taro", "weapp"], default="taro")
    parser.add_argument("--no-backend", action="store_true")
    parser.add_argument("--no-admin", action="store_true")
    parser.add_argument("--no-mini", action="store_true")
    parser.add_argument(
        "--with-a2m",
        action="store_true",
        help="同时生成支付宝 AI 按量付费模块（402 / Payment-Needed / Payment-Proof）",
    )
    args = parser.parse_args()

    target = Path(args.target).resolve()
    target.mkdir(parents=True, exist_ok=True)
    print(f"脚手架输出目录: {target}")

    if not args.no_backend:
        print("[1/3] 生成后端 ai-shop-system ...")
        gen_backend(target, with_a2m=args.with_a2m)
    if not args.no_mini:
        print("[2/3] 生成小程序 ({} ...)".format(args.mini_framework))
        gen_miniprogram(target, args.mini_framework)
    if not args.no_admin:
        print("[3/3] 生成管理后台 shop-admin ...")
        gen_admin(target)

    print("✅ 脚手架生成完成。下一步：按各子目录 README 配置并启动。")


if __name__ == "__main__":
    main()
