# Github入门

## 步骤

### 前置工作

#### 准备一个翻墙工具（非必须）

Github目前处于半墙状态，从国内的访问有时会被限制，可以提高使用体验。

这一步并不是必须的，因为有时候运气好能够访问Github网站。

#### 安装git工具

如果你正在使用Windows系统并计划在Windows下开发，那么请参照这篇[博客](https://blog.csdn.net/qq_40903378/article/details/121028609)下载安装Git，并确保它的二进制文件在你的环境变量中。

如果你正在使用Linux系统并计划在Linux下开发，那么请在终端输入(以Ubuntu为例)：`sudo apt-get install git`

### 正式步骤

#### 注册一个Github帐号

在浏览器中输入网址www.github.com打开，点击“Sign up”，按照提示注册一个Github帐号。

#### 设置SSH

如果你在使用Windows，你可以在任意位置打开Git Bash终端(可以尝试在桌面右键选择"Run Git Bash Here"); 如果你在使用Linux，请直接打开终端输入如下命令

```bash
cd 
git config --global user.name "Your Name" # 填写你的Github用户名
git config --global user.email "Your Email" # 填写你注册用的邮箱
ssh-keygen -t rsa -C "Your Email" # 生成一个ssh key
cd .ssh
cat id_rsa.pub # 打印出你的ssh公钥
```

然后打开Github网页登陆个人账户，在网页右上角点击个人头像，并单击下拉菜单中的Settings，在新打开的网页中点击"SSH and GPG keys"，而后点击"Add SSH key"，将上面获得的公钥复制到Key中，Title可以随意填写。

#### 测试SSH

打开Bash终端，输入如下命令：

```bash
ssh -T git@github.com
```

如果终端输出`Hi <Your Name>! You've successfully authenticated, but GitHub does not provide shell access.`则说明设置成功。

### 拷贝我们的项目

在你想要保存项目位置的地方打开终端，输入

```bash
git clone git@github.com:0xtaruhi/chisel-c910-bpu.git
```

你应该可以看到项目能成功被复制到本地。

如果你已经配置好sbt，尝试在项目根目录下运行`sbt run`，你应该能看到ct_ifu_l0_btb_entry.v文件。后续可以参考项目根目录下的README.md文件，里面有一些信息需要阅读。
