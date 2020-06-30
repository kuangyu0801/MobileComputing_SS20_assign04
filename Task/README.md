# Open Issue
- Done 如何透過marvin連接到Raspberry pi
- Done 如何在Raspberry pi開發並且運行Java
- 如何實踐算法？
- Java Command-line
- Message要用什麼 Class來傳

# Workflow
1. 連上infomatik vpn
2. 連上遠端機器, 設定remote file system
3. 開始編譯java


# Password
team8:Soahu2Ai
$ ssh team8@129.69.210.180

# JDM版本
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)

## 1.VPN to Informatik
- 學校計中下載cisco軟件：https://www.tik.uni-stuttgart.de/en/support/service-manuals/vpn/
- 系上計中步驟http://www.zdi.uni-stuttgart.de/zdi/vpn/anyconnect.html
vpn.informatik.uni-stuttgart.de
- 帳號：example (如果mail是 exmaple@studi.informatik.uni-stuttgart.de)

## 2.Setup remote folder on local computer via SSHFS
1. 用sshfs mount遠端資料夾(可以用local編譯器)比較方便
(ex. sshfs team8@129.69.210.180:/home/team8 remote_shfs)
```
$ sshfs team8@129.69.210.180:/home/team8 #mounting_point
```

2. 輸入密碼 $Soahu2Ai
```
$ Soahu2Ai
```

sshfs安裝教學: 
- [sshfs install on Mac 教學1](https://medium.com/@tzhenghao/writing-remote-code-on-a-mac-with-sshfs-c62d64bf9ef9)
- [sshfs install on Mac 教學2](https://www.jianshu.com/p/c40d135db305)

## 3. 編譯java
1. 建立連線 
```
$ssh team8@129.69.210.180
```
2. 輸入密碼 
```
$ Soahu2Ai
```
- 成功登入後會顯示:
```
Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Thu Jun 25 22:33:33 2020 from 129.69.185.164
```
3. compile 
```
$ javac HelloWorld.java
```
4. run 
```
$ java HelloWorld
```


# Respberry Pi課程
- [Getting Started with Your Raspberry Pi](https://www.futurelearn.com/courses/getting-started-with-your-raspberry-pi)
- [Networking with Python: Socket Programming for Communication](https://www.futurelearn.com/courses/networking-with-python-socket-programming-for-communication)
- [Java Programming on Raspberry Pi - Hello Pi!!!](https://youtu.be/lzeBrm2cGUQ)

# Others
[How to install VNC on Raspberry pi | Remote Access](https://www.youtube.com/watch?v=JZ1pdVVTMrw)
# Java Tutorial
[UDP](https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html)


# Debug
每個node都要能夠把log輸出成文字檔
Log Format(ex log in node B)
- Time -Send/Receive- Message-Content - Message-Header[Source Address, Destination Address, Time Stamp] 
- 1000ms  -Receive  - "Who is my neighbor" - [A, Broadcast, 500ms]
- 3000ms  -Send  - "I'm your neighbor" - [B, A, 3000ms]
- 4000ms  -Send  - "Who is my neighbor" - [B, Broadcast, 4000ms]
- 5000ms  -Send  - "Hello World!"-"ABCDF" - - [B, C, 5000ms]


Message types: NEIGHBOR-REQ, NEIGHBOR-RLY, DSR-REQ, DSR-RLY, DATA
DATA: MSG-HEADER
ex  "Hello World!"-"ABCDF"

# Task 1
1. Implement flooding: 參考老師lec9
2. Discover all nodes and their connections in the network (topology)
    - 是不是代表每個收到的node都要回傳message? sender要能夠辨識然後去除duplicate
3. Send message from any node to all nodes: 
    - 語意不明確, 我們是要透過node-A send message to all node? 
    - 還是說可以透過node-A去involve node-B send message to all node? 
    
4. Draw graph of network including latency between each node
    - 如何測量latency? using timer in java? Use Raspberry Pi System-Timer

# UDP
- server可以透過packet知道client的原始位置

# Task 2
1. Implement DSR
2. Pick any host as a source and send Hello World to all other nodes. (Using Route from DSR)
"Hello World!"
3. No hard-coded or static source
4. How long does route discovery need? (Optional, compare with/out
optimization)
