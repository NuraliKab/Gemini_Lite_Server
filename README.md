# Project Report

Author: Nurali Kabiyev
Email: n.kabiyev@student.maastrichtuniversity.nl
Student ID number: I6356973

## Gemini Lite Client Program
Using command line user can connect to the server and request a file. 

### First of all you need to build the project using Maven:
mvn clean package

### To run the client you need to write this in terminal:
java -cp target/bcs2110-2025.jar gemini_lite.Client <URL> [password]

Example:
java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/hello.txt

After the client sent the request to the server, then server will send of the following responses:
1. 1x status code. if the server will ask for input password then client check if the password was already written in command line argument. If not it asks for the password.
2. 4x status code. The client will wait and then try again to connect to the server.
3. 5x status code. Permanent error occured. The client prints error message and exit with the correct exit code.


### Bonus enhancements

(I have chosen to not implement any bonus enhancements)

## Gemini Lite Server Program

In the server files are stored. Gemini protocol is used to transfer files.

How to run the server:
java -cp target/bcs2110-2025.jar gemini_lite.Server <directory> [<port>]

Example:
java -cp target/bcs2110-2025.jar gemini_lite.Server /home/nurali/ 6671

How server acts:
1. Server have MIME type for each file.
    a. Files ending with ".txt" have meta data "text/plain"
    b. Files ending with ".gmi" have meta data "text/gemini"
    c. Other files have meta data "application/octet-stream"
2. If everything is correct then server returns 20 status code
3. If client asks for directory then server or file do not exist then server returns error code 51
4. It checks if client stay in the root directory
5. It checks if url is too long






### Bonus enhancements

(There is no bonus enhancements)

## Gemini Lite Proxy Program

In my case proxy is middleware between client and server. 

How to run the proxy:
java -cp target/bcs2110-2025.jar gemini_lite.Proxy <port>

Example:
java -cp target/bcs2110-2025.jar gemini_lite.Proxy 6671

How proxy acts:
* Proxy uses ProxyRequestHandler. It reads request from client and sends it to the server. Proxy handles redirects

### Bonus enhancements

(No bonus enhancements)



## Alternative DNS, Bakeoff and Wireshark outputs

### Lab 4 results:
I worked as a non-registrar. My partner and I created subdomain "word.something-nice.lab-kale".

#### DNS configuration

Meaning: This defines a new word.something-nice.lab-kale zone to be a primary zone.
File: /etc/bind/named.conf.local
zone "word.something-nice.lab-kale" {
    type primary;
    file "/var/lib/bind/db.zone.lab-kale";
};

Meaning: This is word.something-nice.lab-kale zone file. It maps domain names to IP addresses
File: /var/lib/bind/db.zone.lab-kale
content of /var/lib/bind/db.zone.lab-kale:
@       IN      SOA     ns1.word.something-nice.lab-kale. hostmaster.word.something-nice.lab-kale. (
                             4         ; Serial
                             10         ; Refresh
                             10         ; Retry
                             10         ; Expire
                             10 )       ; Negative Cache TTL
;
@       IN      NS      ns1.word.something-nice.lab-kale.
ns1     IN      A       10.2.0.128
gemini.word.something-nice.lab-kale. IN A 10.2.0.128
hello.word.something-nice.lab-kale. IN TXT "Hello world!"
nurali.word.something-nice.lab-kale. IN A 10.2.0.128
maarten.word.something-nice.lab-kale. IN A 10.2.0.128


#### Proof that DNS work

Command: dig @127.0.0.1 gemini.word.something-nice.lab-kale

Response:
pi0064@pi0064:~ $ dig @127.0.0.1 gemini.word.something-nice.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @127.0.0.1 gemini.word.something-nice.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 53712
;; flags: qr aa rd; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 1
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 4cdb85bdc48bed4201000000691eff95cb9e4f3138877425 (good)
;; QUESTION SECTION:
;gemini.word.something-nice.lab-kale. IN        A

;; ANSWER SECTION:
gemini.word.something-nice.lab-kale. 10 IN A    10.2.0.128

;; Query time: 4 msec
;; SERVER: 127.0.0.1#53(127.0.0.1) (UDP)
;; WHEN: Thu Nov 20 12:46:29 CET 2025
;; MSG SIZE  rcvd: 108

#### DNS trace
This is the prrof that connection between client and server using dns is working.

1. Find parent server
pi0064@pi0064:~ $ dig ns1.something-nice.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> ns1.something-nice.lab-kale
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 32206
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 1

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 31a10c5f70cdc8fb0100000069283741c618e7bc364588fb (good)
;; QUESTION SECTION:
;ns1.something-nice.lab-kale.   IN      A

;; ANSWER SECTION:
ns1.something-nice.lab-kale. 10 IN      A       10.2.0.133

;; Query time: 15 msec
;; SERVER: 10.2.0.1#53(10.2.0.1) (UDP)
;; WHEN: Thu Nov 27 12:34:25 CET 2025
;; MSG SIZE  rcvd: 100



2. Find child server
pi0064@pi0064:~ $ dig @10.2.0.133 ns1.word.something-nice.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @10.2.0.133 ns1.word.something-nice.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 54095
;; flags: qr rd; QUERY: 1, ANSWER: 0, AUTHORITY: 1, ADDITIONAL: 2
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 06171171ae48b8070100000069283748ac416ac1722ad5a8 (good)
;; QUESTION SECTION:
;ns1.word.something-nice.lab-kale. IN   A

;; AUTHORITY SECTION:
word.something-nice.lab-kale. 10 IN     NS      ns1.word.something-nice.lab-kale.

;; ADDITIONAL SECTION:
ns1.word.something-nice.lab-kale. 10 IN A       10.2.0.128

;; Query time: 35 msec
;; SERVER: 10.2.0.133#53(10.2.0.133) (UDP)
;; WHEN: Thu Nov 27 12:34:32 CET 2025
;; MSG SIZE  rcvd: 119



3. Verify child server
pi0064@pi0064:~ $ dig @10.2.0.128 gemini.word.something-nice.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @10.2.0.128 gemini.word.something-nice.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 2633
;; flags: qr aa rd; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 1
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 54048b384e47a40701000000692837638db40b7b2c2a23e7 (good)
;; QUESTION SECTION:
;gemini.word.something-nice.lab-kale. IN        A

;; ANSWER SECTION:
gemini.word.something-nice.lab-kale. 10 IN A    10.2.0.128

;; Query time: 0 msec
;; SERVER: 10.2.0.128#53(10.2.0.128) (UDP)
;; WHEN: Thu Nov 27 12:34:59 CET 2025
;; MSG SIZE  rcvd: 108




### Lab 5 results:
I used WireShark and nmap to analyze live traffic on kale network. The focus is to understand how TCP manages connection, ensure data delivery and terminates session between client and server.

This is the trace captured during the lab

#### Trace 1: traffic between client and server

|**No.**|**Time**|**Source**|**Destination**|**Protocol**|**Length**|**Info**|
|---|---|---|---|---|---|---|
|**440**|17.803512|10.2.1.250|10.2.0.128|TCP|66|25732 → 1958 [SYN] Seq=0 Win=65535 Len=0|
|**442**|17.823391|10.2.0.128|10.2.1.250|TCP|66|1958 → 25732 [SYN, ACK] Seq=0 Ack=1 Win=32120 Len=0|
|**443**|17.823528|10.2.1.250|10.2.0.128|TCP|54|25732 → 1958 [ACK] Seq=1 Ack=1 Win=65280 Len=0|
|**444**|17.827315|10.2.1.250|10.2.0.128|TCP|95|25732 → 1958 [PSH, ACK] Seq=1 Ack=1 Win=65280 **Len=41**|
|**445**|17.830669|10.2.0.128|10.2.1.250|TCP|54|1958 → 25732 [ACK] Seq=1 **Ack=42** Win=32128 Len=0|
|**446**|17.830669|10.2.0.128|10.2.1.250|TCP|70|1958 → 25732 [PSH, ACK] Seq=1 Ack=42 Win=32128 Len=16|
|**447**|17.830669|10.2.0.128|10.2.1.250|TCP|72|1958 → 25732 [FIN, PSH, ACK] Seq=17 Ack=42 Win=32128 Len=18|
|**448**|17.830727|10.2.1.250|10.2.0.128|TCP|54|25732 → 1958 [ACK] Seq=42 Ack=36 Win=65280 Len=0|
|**449**|17.831591|10.2.1.250|10.2.0.128|TCP|54|25732 → 1958 [FIN, ACK] Seq=42 Ack=36 Win=65280 Len=0|
|**450**|17.834393|10.2.0.128|10.2.1.250|TCP|54|1958 → 25732 [ACK] Seq=36 Ack=43 Win=32128 Len=0|

Analysis:
1. Packets 440 and 443 show the standard three way handshake
2. Packet 444 show that Client sending the request. Packet 445 is ACK from server.
3. The server split response into two packets 446 and 447.
4. The FIN 447 packet is sent by client to close the connection.



#### Sever Response
|**No.**|**Time**|**Source**|**Destination**|**Protocol**|**Length**|**Info**|
|---|---|---|---|---|---|---|
|**46**|4.118514|10.2.1.250|10.2.0.101|TCP|66|31329 → 1958 [SYN] Seq=0 Win=65535 Len=0|
|**47**|4.122402|10.2.0.101|10.2.1.250|TCP|66|1958 → 31329 [SYN, ACK] Seq=0 Ack=1 Win=32120 Len=0|
|**48**|4.122535|10.2.1.250|10.2.0.101|TCP|54|31329 → 1958 [ACK] Seq=1 Ack=1 Win=65280 Len=0|
|**49**|4.125875|10.2.1.250|10.2.0.101|TCP|104|31329 → 1958 [PSH, ACK] Seq=1 Ack=1 Win=65280 **Len=50**|
|**50**|4.129278|10.2.0.101|10.2.1.250|TCP|54|1958 → 31329 [ACK] Seq=1 **Ack=51** Win=32128 Len=0|
|**51**|4.130898|10.2.0.101|10.2.1.250|TCP|120|1958 → 31329 [PSH, ACK] Seq=1 Ack=51 Win=32128 Len=66|
|**52**|4.130898|10.2.0.101|10.2.1.250|TCP|54|1958 → 31329 [FIN, ACK] Seq=67 Ack=51 Win=32128 Len=0|
|**53**|4.130982|10.2.1.250|10.2.0.101|TCP|54|31329 → 1958 [ACK] Seq=51 Ack=68 Win=65280 Len=0|
|**54**|4.132314|10.2.1.250|10.2.0.101|TCP|54|31329 → 1958 [FIN, ACK] Seq=51 Ack=68 Win=65280 Len=0|
|**55**|4.135993|10.2.0.101|10.2.1.250|TCP|54|1958 → 31329 [ACK] Seq=68 Ack=52 Win=32128 Len=0|

Analysis:
1. Packets 46 and 48 show the three way handshake
2. Packet 49 show that Client sending the request. Packet 50 is ACK from server.
3. Server send response using 51 packet
4. The FIN 52 packet is sent by client to close the connection




## Reflection on Gemini Lite

Gemini uses 2 digit status code, while http uses 3 digit status codes like 500, 404. Gemini is intented to be simple version of HTTP. In gemini firs digit tell about kind of response, second digit tell us details about response. This way it is easier for client to identify kind of response. Another difference is that caching proxy is hard to implement in gemini. But HTTP has built-in features to implement caching proxy. 

Disadvantages of gemini lite is efficiency. This protocol requires TCP conection to be established for each request. So if page have many resources, it will connect and disconnect many times.