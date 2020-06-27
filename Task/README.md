# Open Issue
- Done 如何透過marvin連接到Raspberry pi
- Done 如何在Raspberry pi開發並且運行Java
- 如何實踐算法？
- Java Command-line

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
1. $ sshfs team8@129.69.210.180:/home/team8 #mounting_point
(ex. sshfs team8@129.69.210.180:/home/team8 remote_shfs)
2. 輸入密碼 $Soahu2Ai

sshfs安裝教學: 
- [sshfs install on Mac 教學1](https://medium.com/@tzhenghao/writing-remote-code-on-a-mac-with-sshfs-c62d64bf9ef9)
- [sshfs install on Mac 教學2](https://www.jianshu.com/p/c40d135db305)

## 3. 編譯java
1. 建立連線 
```
$ssh team8@129.69.210.180
```
2. 輸入密碼 $Soahu2Ai
- 成功登入後會顯示:
```
Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Thu Jun 25 22:33:33 2020 from 129.69.185.164
```
3. compile $javac HelloWorld.java
4. run $java HelloWorld


# Respberry Pi課程
- [Getting Started with Your Raspberry Pi](https://www.futurelearn.com/courses/getting-started-with-your-raspberry-pi)
- [Networking with Python: Socket Programming for Communication](https://www.futurelearn.com/courses/networking-with-python-socket-programming-for-communication)
- [Java Programming on Raspberry Pi - Hello Pi!!!](https://youtu.be/lzeBrm2cGUQ)

# Others
[How to install VNC on Raspberry pi | Remote Access](https://www.youtube.com/watch?v=JZ1pdVVTMrw)
