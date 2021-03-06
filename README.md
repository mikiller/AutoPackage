# 多渠道打包实现

### 说明
* 参考Github上yipianfengye（刘超）前辈的多渠道打包脚本，在其基础上略作修改，并结合之前autopackage2.0自动打包逻辑，发布此3.0版本
* 在原作多渠道打包的基础上，加入对母包的签名、对齐等工作。
* 以及通过终端交互，动态设置渠道配置文件、母包、keystore、输出目录等路径。
* 对母包签名主要是由于直接用ide打出签名包后可能需要使用加密工具（平台）对母包进行加密后，再打渠道包。此时母包需要重签名。

### 使用说明
* 首先需要安装python环境，生成渠道包的脚本是python脚本，我这里安装的是python3.5，不知道如何安装的，请自行百度。。。
* 打包一个普通的apk，我们是在这个渠道包的基础上更改其内容，生成其他渠道包的。
* 运行autopackage3.0.py脚本，根据提示输入所需路径，然后生成不同的渠道包。
* 最后提示“输入渠道包路径”可任意输入，默认生成路径为脚本当前所在文件夹。若路径不存在，将自动创建。

### 注意
* keystore文件必须与autop3.0.py脚本放在同级目录，否则jarsigner会报错
* 为了方便，这里要求keystore文件的align名称必须与keystore文件同名。否则jarsigner签名失败。
* 若不得不使用align名与文件名不同的keystore，请自行修改脚本代码。

### 目录文件
* app-release.apk是由Android Studio直接生成的demo签名包。
* app-release.encrypted.apk是经过360加固后的demo未签名母包。
* autopa3.0.py 多渠道自动打包脚本
* ChannelUtil.java 在Android项目中使用该工具类获取渠道号
* README.md
