package com.lzh.dhtserver.logic.schedule;

import com.lzh.dhtserver.logic.handler.DHTServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * 定时检测本地节点数并自动加入 DHT 网络
 *
 **/
@Slf4j
@Component
public class AutoJoinDHT {

	@Autowired
	private DHTServerHandler handler;

	@Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
	public void doJob() {
		if (handler.NODES_QUEUE.isEmpty()) {
			log.info("本地 DHT 节点数为0，自动重新加入 DHT 网络..");
			handler.joinDHT();
		}
	}
}
