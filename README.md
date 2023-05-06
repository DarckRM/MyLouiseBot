# MyLouise Bot

![](https://img.shields.io/badge/go--cqhttp-v1.0.0-green)![](https://img.shields.io/badge/mysql-v8.0.31-blue)![](https://img.shields.io/badge/naiveui-v2.34.3-brightgreen)![](https://img.shields.io/badge/openjdk19-LTS-orange)![](https://img.shields.io/badge/louise-dev-ff69b4)

## 简介

基于Java JDK19，Spring Boot，MySQL, DragonFly 实现的 API 应用，内部实现了 go-cqhttp 的部分接口请求方法，提供了 HTTP 服务和 WebSocket 服务，以及一些内置的功能用于搭建实现了 One Bot 协议的机器人

除此之外，系统提供了插件的开发接口，支持插件开发和运行时加载2.34.3

![img](https://camo.githubusercontent.com/f2f2db129b746f3c8b85c84ed3f16bf376fb46db5680ff1da401e98f418a9b8d/68747470733a2f2f73322e6c6f6c692e6e65742f323032322f30342f31332f4138355a77763675316265454459742e706e67)

## 快速上手

1. 安装 Open JDK 19
   
   > MyLouise 最近开始尝试升级 JDK 以支持最新的功能，`0.1.7-dev` 版本是支持 JDK 11 的，后续版本还是请安装推荐的 JDK

2. 安装 MySQL 并导入基础的表结构

   SQL 文件已在项目中提供，你可以通过 `git clone` 本项目，或是在 `release` 页面下载的压缩包中找到文件

   > 目前 MyLouise 处于频繁更新的状态，新版本的发布，可能会对表结构有所改变，请注意备份你的数据

3. 安装 DragonFly 数据库

   这是一个类似 Redis 的数据库，用于缓存使用，在 `0.1.7-dev` 版本中已经被使用

4. 下载最新分发文件

   在 `release` 页下载文件，内部包括了 BOT 部分以及前端部分，如果没有前端部分你也可以前往仓库 `NaiveSaito` 仓库进行下载

5. 运行~

   将下载的分发文件解压缩，使用命令 `java -jar xxx.jar` 以运行 BOT
