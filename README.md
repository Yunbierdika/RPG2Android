# RPG2Android

可用于把使用 RPG Maker 制作的 PC 游戏（不使用 Html 的版本如 VX Ace 不支持，建议改成 Joiplay 模拟器运行）打包成安卓版。

使用 Kotlin 语言风格重写了之前 Java 版的[妹相随安卓版打包框架](https://github.com/Yunbierdika/ImotoFantasyAndroidFramework)项目 ，此外，使用了 WebViewAssetLoader 代替 WebView 被标记为弃用的方法：allowUniversalAccessFromFileURLs。

### 特性

- 点击游戏中存档的保存按钮后，存档将直接保存到 **_Android/data/game.YourGameName/files/save/_** 目录下，并且存档可以和 PC 版互通。

- 不需要获取任何权限，非 ROOT 用户可以直接进入 **_Android/data/game.YourGameName/files/save/_** 目录下对存档进行导入、导出操作。

- 游戏出现错误时会把错误记录到 **_Android/data/game.YourGameName/files/logs.txt_** 文件中。

### 使用方法

1. 将游戏资源存放到 **_app/src/main/assets/_** 目录下（RPG 游戏目录下的'www'文件夹内的文件，不包含'www'文件夹）后打包即可（注意不要替换掉 js 文件夹中的文件）

2. 将项目中的 “rpg_managers.js” 文件中的代码复制**替换**掉你的 RPGM 游戏里同文件名的文件中的代码。

3. 将 **_app/src/build.gradle.kts_** 中的 **applicationId = "game.YourGameName"** 改成你自己的游戏名称（英文）比如：**game.imotofantasy**，用于显示路径包名；将 **_app/src/main/res/values/strings.xml_** 中的 **YourGameName** 改成你自己的游戏名称，用于显示 App 的名称。

4. 使用 Android Studio 打开项目，删除 **_app/src/main/res_** 目录下的 mipmap ，然后右键 **_app/src/main/res_** 点击 **“New -> Image Asset”** 添加你自己的游戏图标，图标的 **“Name”** 需要和 **_app/src/main/AndroidManifest.xml_** 中的 **“android:icon”**、**“android:roundIcon”** 一致。 最后编译即可。
