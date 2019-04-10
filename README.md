# Aria2Server

Aria2 是一个类似于 Linux 下的 wget 下载工具，不过比 wget 高明得不知到哪去了，支持 HTTP（S），（S）FTP（S），以及众多的 BT 协议。

Aria2 如果像 wget 那样传递一个 url 就可以把文件下载回来，但是，Aria2 还有个吊炸天的功能，支持服务端模式运行。比如最近网上很多教程，指导大家在国外的 VPS（云主机）上安装 Aria2，然后启动 RPC 服务，再使用 WEB 管理工具连上你的 Aria2 RPC 服务器。

上面说到的，网上已经找到很多文章了，比如这个：https://www.moerats.com/archives/401/

我这里是把 Aria2  ARM 编译版本，移到安卓上，上面的 VPS 就是接下来你的手机。为什么会有这个需求呢，很简单，比如你买不起服务器，就算你买得起服务器也买不起带宽；你买不起 NAS，买不起可刷固件的路由器，或者你觉得这很难，但是你迫切需要一个 24 小时开机，放在家里的可 24 小时下载的 “服务器”，你可以通过你的电脑，平板管理这个 “服务器” …

安装后，点击右上三个白点，进入设置：

![](http://euch.gotoip1.com/uploads/74206134-fb10-4935-b755-cf11cffee6d8.png)

这里有个关键项目，RPC-Secret ，这是你的 Aria2 服务器密钥，你也可以不用改！

点击小三角形启动服务器。

![](http://euch.gotoip1.com/uploads/ad189e45-e222-45f8-b756-ff5dc44d325a.png)

然后在电脑上打开我搭建的 Aria-ng : http://euch.gotoip1.com/aria-ng/

![](http://euch.gotoip1.com/uploads/c17207a6-5139-442f-b3b1-714c7f18e3dd.png)

依次点击 1、2 进入上图窗口，在 3 处输入你的手机的 IP，在 4 处输入 RPC-Secret，如果你没有修改过，那就是 123456

然后刷新页面，会提示你连接成功：

![](http://euch.gotoip1.com/uploads/02cf376d-16dc-495d-9a1a-b866437b7dab.png)

接下来我们去添加下载：

![](http://euch.gotoip1.com/uploads/2d638a89-5586-4d5a-8062-ece95b738d1c.png)

然后你要下载的文件，就会在你手机进行下载了。

![](http://euch.gotoip1.com/uploads/2c9e0a42-734b-4536-8ef8-a9e437c56f05.png)

有同学问我，为什么不做个直接下载的，我想说的是这类软件特别多，优秀的有 IDM+ 和 ADM 都是直接的下载软件，如果确实想作为客户端用的话，可以安装 Aria2客户端，酷安搜索 Aria远程下载，然后配置服务器为本手机的IP就可以了，这样你手机即作为服务端也作为客户端了 。