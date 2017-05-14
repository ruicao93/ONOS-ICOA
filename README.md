## 一. ICOA项目描述

ICOA全称：Inter Controller Coorperation ONOS Application

以东西向协议OXP作为支持在ONOS上开发APP，支持异构控制器之间通过OXP进行跨SDN域的协同工作。修改了部分ONOS源码，在后期优化中，逐渐消除对ONOS源码的修改，尽量作为一个独立的APP.

## 二. ONOS简介

### What is ONOS?
ONOS is a new SDN network operating system designed for high availability,
performance, scale-out.

### Top-Level Features

* High availability through clustering and distributed state management.
* Scalability through clustering and sharding of network device control.
* Performance that is good for a first release, and which has an architecture
  that will continue to support improvements.
* Northbound abstractions for a global network view, network graph, and
  application intents.
* Pluggable southbound for support of OpenFlow and new or legacy protocols.
* Graphical user interface to view multi-layer topologies and inspect elements
  of the topology.
* REST API for access to Northbound abstractions as well as CLI commands.
* CLI for debugging.
* Support for both proactive and reactive flow setup.
* SDN-IP application to support interworking with traditional IP networks
  controlled by distributed routing protocols such as BGP.
* IP-Optical use case demonstration.

Checkout out our [website](http://www.onosproject.org) and our
[tools](http://www.onosproject.org/software/#tools)

## 三. 安装教程
#### 1. 下载OXP源码，并切换到最新版本(v2)

```
git clone https://github.com/paradisecr/ONOS-ICOA.git
mv ONOS-ICOA onos
git checkout v2
```

##### 2. 安装ONOS依赖：
方法一：使用ONOS自带脚本安装
```
cd onos
./tools/dev/bin/onos-setup-ubuntu-devenv
```

方法二：
手动安装依赖，参照ONOS wiki

#### 3. 添加ONOS相关环境变量

```
vim ~/.profile
```

在末尾追加内容:
```
. ~/onos/tools/dev/bash_profile
```

使环境变量生效：

```
source ~/.profile
```
#### 4. 编译ONOS

为使编译顺利，推荐使用国内Maven镜像源。

一种可选的做法(使用阿里云Maven镜像,代替central镜像)：在maven的settings.xml 文件里配置mirrors的子节点，添加如下mirror:
```
<mirror>
        <id>nexus-aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror> 
```
编译ONOS源码, 耐心等待安装完成即可：

```
cd ~/onos
mvn clean install -DskipTests

等待安装完成：
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 08:29 min
[INFO] Finished at: 2017-05-14T17:17:48+08:00
[INFO] Final Memory: 341M/621M
[INFO] ------------------------------------------------------------------------
```
编译可能会遇到的错误：
```
1. /usr/lib/jvm/jdk1.8.0_112/jre/lib/amd64/libawt_xawt.so: libXrender.so.1: cannot open shared object file: No such file or directory
当运行环境为server版本时，缺少图形库。安装必要的图形库即可。
网上推荐的方法(未测试)：http://www.th7.cn/Program/java/201612/1054332.shtml
我的方法(投机取巧，安装ubuntu的图形化记事本gedit，它会帮忙安装所有图形库依赖)：sudo apt install gedit
```
#### 5. 启动ONOS

```
ok clean
```
启动成功的提示：
```
Welcome to Open Network Operating System (ONOS)!
     ____  _  ______  ____     
    / __ \/ |/ / __ \/ __/   
   / /_/ /    / /_/ /\ \     
   \____/_/|_/\____/___/     
                               
Documentation: wiki.onosproject.org      
Tutorials:     tutorials.onosproject.org 
Mailing lists: lists.onosproject.org     

Come help out! Find out how at: contribute.onosproject.org 

Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.
Hit '<ctrl-d>' or type 'system:shutdown' or 'logout' to shutdown ONOS.

onos> summary 
node=127.0.0.1, version=1.6.0
nodes=1, devices=0, links=0, hosts=0, SCC(s)=0, flows=0, intents=0
onos>
```
