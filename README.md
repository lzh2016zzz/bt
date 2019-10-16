# bt

#### 介绍
本项目是一个基于Netty和Spring框架开发的BT搜索引擎和DHT爬虫系统。

它的核心功能是模拟DHT节点，以此在P2P下载网络中嗅探并收集BT torrent的数据信息。这些数据随后被储存在数据库中，从而实现对全球正在下载的BT种子信息的跟踪和索引。

简单地说,就是拥有这个服务,就可以对全球正在下载哪些BT种子进行跟踪和索引 同时可以对收集到的数据进行检索


#### 技术 

Netty,Kafka,Spring web,Dht协议,UDP

#### 项目结构

 - bt-api 查询API 用于查询收集到的BT-torrent 使用springweb开发
 - bt-common 工具类 通用代码 
 - bt-exchange 数据存储 将收集到的BT-torrent 推送到kafka 消费者从kafka消费数据并写入
 - dht-server DHT服务


#### 支持的BEP协议列表

- BEP-5: DHT Protocol DHT协议，是项目嗅探P2P网络的基础
- BEP-9: Extension for Peers to Send Metadata Files 允许对等节点发送元数据文件的扩展
- BEP-10: Extension Protocol 扩展协议

