# apple-store-listener

## 简介

## 技术

1. Vertx 用于线程通信。
2. MongoDB 用于存储数据。
3. SpringBoot 

## 疑难问题

1. Apple Store 网页的内容变换不定。

## 经验教训

1. 访问翻新网站，最好使用 Safari，或者把 User-Agent 改为 Safari 的配置。不知道 Apple Store 是否针对 User-Agent 做了选择性的行为。
2. Vertx 其实也就那样，像这种小东西没必要用 Vertx 搞得有板有眼的。一把梭就行了。