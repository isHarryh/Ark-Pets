ArkPets附加说明文档
# 命令行启动

ArkPets的命令行启动方法，目前只支持一个命令行参数。

### 前提
必须正确安装Java运行环境(JRE)，且正确地配置到了`PATH`环境变量中。  
必须将命令行工作目录(cd)设置为`.jar`文件所在的目录，否则可能找不到模型。

### 用法
```
[java -jar] <.jar程序文件的完整名称> [--direct-start]
```
* `java -jar`     使用此前缀运行将会打印调试信息。
* `--direct-start`  直接启动桌宠而不打开启动器。(v1.5.1+)

### 用例
```shell
cd /d D:\MyArkPets
java -jar ArkPets-v1.5.1.jar --direct-start
```
```shell
cd /d D:\MyArkPets
ArkPets-v1.5.1.jar --direct-start
```
