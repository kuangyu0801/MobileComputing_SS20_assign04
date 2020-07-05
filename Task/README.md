# 常用command
```
javac PiUDPServer.java

java PiUDPServer
javac PiUDPClient.java
java PiUDPClient 3

javac DSRServer.java
java DSRServer 1 

javac DSRClient.java
java DSRClient 1 1
java DSRClient 1 4
```
# Network TOPOLOGY
```
196-197-180-174
     |   |    |
     |- 185 - |

final static String[] ALL_ADDRS = {
            "192.168.210.174", "192.168.210.180", "192.168.210.185",
            "192.168.210.196", "192.168.210.197"};
 ```    
# Open Issue
1. 使用以下只會得到TCP Loopback interface, 是不是要用etho0的ip位置當log file才比較好
2. 如何實踐算法？
```  
InetAddress localhost = InetAddress.getLocalHost();
localAddress = localhost.getHostAddress();
Display name: TCP Loopback interface
Name: lo
InetAddress: /127.0.0.1
```
## Done
- 如何透過marvin連接到Raspberry pi? vpn連到記中再ssh到不同的機器
- 如何在Raspberry pi開發並且運行Java? 用remote filesystem 把java file從local複製過去, 執行compile, execute
- Done Java Command-line
- Done Message要用什麼 Class來傳? 用String就好！
- Broadcast收不到? 因為port沒有設好, 導致說第二層broadcast會傳錯port
- 如何量delay round trip time? 相鄰的點互相broadcast手動紀錄，時間差是用network clock的時間計算(Raspbian gets the time from an NTP Server (a "time server").)

# Password
team8:Soahu2Ai
$ ssh team8@129.69.210.180

# Connection
196-197

# Workflow
1. 連上infomatik vpn
2. 連上遠端機器, 設定remote file system
3. 開始編譯java

## 1.VPN to Informatik
- 學校計中下載cisco軟件：https://www.tik.uni-stuttgart.de/en/support/service-manuals/vpn/
- 系上計中步驟http://www.zdi.uni-stuttgart.de/zdi/vpn/anyconnect.html
```
vpn.informatik.uni-stuttgart.de
```
- 帳號：example (如果mail是 exmaple@studi.informatik.uni-stuttgart.de)

## 2.Setup remote folder on local computer via SSHFS
1. 用sshfs mount遠端資料夾(可以用local編譯器)比較方便
(ex. sshfs team8@129.69.210.180:/home/team8 remote_shfs)
```
$ sshfs team8@129.69.210.180:/home/team8 #mounting_point
// example
$ sshfs team8@129.69.210.180:/home/team8 remote_180
sshfs team8@129.69.210.174:/home/team8 remote_174
sshfs team8@129.69.210.180:/home/team8 remote_180
sshfs team8@129.69.210.185:/home/team8 remote_185
sshfs team8@129.69.210.196:/home/team8 remote_196
sshfs team8@129.69.210.197:/home/team8 remote_197
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
ssh team8@129.69.210.185
ssh team8@129.69.210.174
ssh team8@129.69.210.196
ssh team8@129.69.210.197
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



# 參考
某屆大神答案
https://github.com/GeorgeEskandar/Ad-Hoc-Network-routing-algorithm
是使用hard-code的方式, 每台機器都綁定一份專屬自己ip的code(非常傷神), 直接從網卡parse data就可以！


# Respberry Pi課程
- [Getting Started with Your Raspberry Pi](https://www.futurelearn.com/courses/getting-started-with-your-raspberry-pi)
- [Networking with Python: Socket Programming for Communication](https://www.futurelearn.com/courses/networking-with-python-socket-programming-for-communication)
- [Java Programming on Raspberry Pi - Hello Pi!!!](https://youtu.be/lzeBrm2cGUQ)

# Others
[How to install VNC on Raspberry pi | Remote Access](https://www.youtube.com/watch?v=JZ1pdVVTMrw)
[Linux 常用網路指令](http://linux.vbird.org/linux_server/0140networkcommand.php#network_setup_ip)

# Java Tutorial
- [Working with UDP Datagram Sockets](https://www.geeksforgeeks.org/working-udp-datagramsockets-java/)
- [DatagramPacket](https://www.geeksforgeeks.org/java-net-datagrampacket-class-java/)
- [UDP](https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html)
- [Getting one's IP](https://www.geeksforgeeks.org/java-program-find-ip-address-computer/)
- [Listing Network Interface Addresses](https://docs.oracle.com/javase/tutorial/networking/nifs/listing.html)
## JDM版本
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)

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
    - 相鄰的點互相broadcast手動紀錄，時間差是用network clock的時間計算(Raspbian gets the time from an NTP Server (a "time server").)

# UDP
- server可以透過packet知道client的原始位置

# Task 2
1. Implement DSR
2. Pick any host as a source and send Hello World to all other nodes. (Using Route from DSR)
"Hello World!"
3. No hard-coded or static source
4. How long does route discovery need? (Optional, compare with/out
optimization)


# DSR Algorithm

```

void DSR() {
	//format: type, src, dst, time, msg, path
	// RREQ, S, D, 999, Hello World, 

	// S ->E -> F - > J -> D
	if (!isDuplicate(receiveData)){
		if (receiveData.type == RREQ) {
	        // reach destination D and only reply the first received RREQ
	        if(receiveData.dst == myIP) {
	            // add dst to path and send back RREP to src
	            sendData = new Data();

	            // {S_E_F_J_D}
	            sendData.path = receiveData.path().add(myIP);
	            sendData.src = myIP;
	            sendData.dst = receiveData.src;
	            directSend(sendData, receiveData.path().pop());
	            // (S, reverse{S_E_F_J_D})
	            pathMap.put(sendData.dst, sendData.path);
	        } else { // not destination, broadcast
	        	sendData = new Data(receiveData);
	        	sendData.path = receiveData.path().add(myIP);
	        	broadcast(sendData);
		    }

		} else if (type == RREP) {
			if (receiveData.dst == myIP) {
				// add path to map
				// (D, {S_E_F_J_D})
				pathMap.put(receiveData.src, receiveData.path);
			} else {
				// ex. we are F, {S_E_F_J_D} -> E
				next = findFowardReversePath(receiveData.path(), myIP);
				// forward message
				directSend(receiveData, next);
			}

		} else { // DATA
			if (receiveData.dst == myIP) {
				// do-nothing
			} else {
				// ex. we are F, {S_E_F_J_D} -> J
				next = findFowardPath(receiveData.path(), myIP);
				// forward message
				directSend(receiveData, next);
			}
		} 
	}

}

class Client {
	public static void Main() {
		sendData = new Data();
		sendData.dst = D;
		if (pathMap.containsKey(sendData.dst) {
			// pathMap.get(sendData.dst) == {S_E_F_J_D}
			next = findFowardPath(pathMap.get(sendData.dst), myIP);
			directSend(sendData, next)
		} else {
			broadcast(RREQ, sendData.dst);
			while (true) {
				// waiting for DSR result;
				DSR();
				if(pathMap.containsKey(sendData.dst)) {
					break;
				}
			}
			next = findFowardPath(pathMap.get(sendData.dst), myIP);
			directSend(sendData, next);
		}
	}
}

class Server {
	public static void Main() {
		while (true) {
			DSR();
		}
	}	
}

// pathMap (dst, pathToDst)

boolean isDuplicate(Data data) {
	// header: type + src + dst + time + msg 
	Header header = new Header(data);
	if(receivedHeaderSet.contains(header)) {
		return false;
	} else {
		receivedHeaderSet.put(header);
	return false;
}


```

