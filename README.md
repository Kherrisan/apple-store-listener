# apple-store-listener

## 简介

### Apple Store 翻新区的上新监视器

Apple Store 的翻新区会不定期上架一些翻新的商品，此监视器能够定时检测苹果商店的网页，当有翻新商品上架时，会向指定邮箱发送邮件，即时提醒用户去抢购。

> 在iPad Pro贴吧逛了逛之后发现，抢翻新的人还是有点多的。

**虽然功能是基本完成的，但考虑到可能出现的各种奇奇怪怪的原因，本程序不做任何承诺和保证。**

[Demo](https://asl.kherrisan.cn)

## 有且只有唯一的界面

![](https://raw.githubusercontent.com/Kherrisan/apple-store-listener/master/page.png)

## 技术

1. Vertx 用于线程通信。
2. MongoDB 用于存储数据。
3. SpringBoot 

## 疑难问题

1. Apple Store 网页的内容变换不定，至今都没有找到原因。

## 经验教训

1. 访问翻新网站，最好使用 Safari，或者把 User-Agent 改为 Safari 的配置。不知道 Apple Store 是否针对 User-Agent 做了选择性的行为。
2. Vertx 其实也就那样，像这种小东西没必要用 Vertx 搞得有板有眼的。一把梭就行了。

<a href="https://www.buymeacoffee.com/Kherrisan" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" style="height: 51px !important;width: 217px !important;" ></a>