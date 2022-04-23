# 基于Chisel的玄铁C910处理器分支预测部分的实现

## 项目背景

该项目是2022年复旦大学嵌入式处理器与芯片系统设计(H)课程的一个实验项目。

## 如何使用本项目

### 安装必要的工具链

本项目需要安装的工具包括：

- [scala sbt](https://www.scala-sbt.org/download.html)
- [chisel3](https://github.com/chipsalliance/chisel3)
- 玄铁相关工具链

### 克隆代码到本地

`git clone https://github.com/0xtaruhi/chisel-c910-bpu.git`

### 运行

在项目根目录下输入`sbt run`，即可于gen_rtl文件夹中生成本项目的Verilog代码。

### 端口名重命名

由于Chisel生成的端口名往往带有前缀io_，在util/文件夹中有一个脚本文件可以将这些前缀去掉。在该文件下运行`make`即可把修改所有位于gen_rtl中的verilog文件内容。

**重要**：在Chisel中自动添加io_前缀是为了避免和内部命名冲突。所以要使用这个脚本，**请确保你写的Chisel代码中的内部命名不与端口名冲突**。

当然，你也可以不执行这一步，但你需要在C910的源代码中修改例化相关代码，使得例化时拥有io_前缀。

### 测试

利用smart平台(本项目不提供)测试生成的Verilog代码能够替换玄铁中的相应模块并运行。

## 相关项目

- [openc910](https://github.com/T-head-Semi/openc910)

## 主要项目负责人

2020级复旦大学微电子科学与工程分支预测小组

## 参与贡献方式

下载项目到本地后，添加或修改项目文件，跑通后提交Pull Request，代码审核通过后会合并到主分支。组内成员会被添加至项目贡献者，修改完成后可以使用如下命令

```bash
git add ./* 
# 上一步用于增加追踪(或暂存更改)所有文件(不包括.gitignore忽略的文件)，确保你追踪的文件不含临时文件或运行库文件等不必要文件。
# 你也可以使用git add file仅添加或暂存某个文件
# 如果你希望总是忽略某些文件，可以修改.gitignore
git commit -m "<message>：本次提交的信息，参考下面的commit规范"
git push
```

**重要**：所有的代码注释必须使用英语，变量命名、函数命名、类命名也都必须使用英语，否则会被拒绝。

### 相关学习参考资料

- [commit规范](https://www.cnblogs.com/chucklu/p/10400519.html)

## 开源协议
