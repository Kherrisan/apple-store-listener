# apple-store-listener

## 简介

### Apple Store 的翻新区上新监视器

Apple Store 的翻新区会不定期上架一些翻新的商品，此监视器能够定时检测苹果商店的网页，当有翻新商品上架时，会向指定邮箱发送邮件，即时提醒用户去抢购。

> 在iPad Pro贴吧逛了逛之后发现，抢翻新的人还是有点多的。

![Demo]()

## 界面

![](https://raw.githubusercontent.com/Kherrisan/apple-store-listener/master/page.png)

## 技术

1. Vertx 用于线程通信。
2. MongoDB 用于存储数据。
3. SpringBoot 

## 疑难问题

1. Apple Store 网页的内容变换不定。

## 经验教训

1. 访问翻新网站，最好使用 Safari，或者把 User-Agent 改为 Safari 的配置。不知道 Apple Store 是否针对 User-Agent 做了选择性的行为。
2. Vertx 其实也就那样，像这种小东西没必要用 Vertx 搞得有板有眼的。一把梭就行了。

<style>
.bmc-button img{height: 34px !important;width: 35px !important;margin-bottom: 1px !important;box-shadow: none !important;border: none !important;vertical-align: middle !important;}.bmc-button{padding: 7px 10px 7px 10px !important;line-height: 35px !important;height:51px !important;min-width:217px !important;text-decoration: none !important;display:inline-flex !important;color:#FFFFFF !important;background-color:#FF813F !important;border-radius: 5px !important;border: 1px solid transparent !important;padding: 7px 10px 7px 10px !important;font-size: 22px !important;letter-spacing: 0.6px !important;box-shadow: 0px 1px 2px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 1px 2px 2px rgba(190, 190, 190, 0.5) !important;margin: 0 auto !important;font-family:'Cookie', cursive !important;-webkit-box-sizing: border-box !important;box-sizing: border-box !important;-o-transition: 0.3s all linear !important;-webkit-transition: 0.3s all linear !important;-moz-transition: 0.3s all linear !important;-ms-transition: 0.3s all linear !important;transition: 0.3s all linear !important;}.bmc-button:hover, .bmc-button:active, .bmc-button:focus {-webkit-box-shadow: 0px 1px 2px 2px rgba(190, 190, 190, 0.5) !important;text-decoration: none !important;box-shadow: 0px 1px 2px 2px rgba(190, 190, 190, 0.5) !important;opacity: 0.85 !important;color:#FFFFFF !important;}
</style>
<link href="https://fonts.googleapis.com/css?family=Cookie" rel="stylesheet">
<a class="bmc-button" target="_blank" href="https://www.buymeacoffee.com/Kherrisan">
    <img src="https://cdn.buymeacoffee.com/buttons/bmc-new-btn-logo.svg" alt="Buy me a coffee">
    <span style="margin-left:15px;font-size:28px !important;">Buy me a coffee</span>
</a>