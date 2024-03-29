package com.lzh.dhtserver.logic.entity;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 去重的节点阻塞队列
 *
 **/
public class UniqueBlockingQueue {

    private Set<String> ips = new ConcurrentSkipListSet<>();
    private BlockingQueue<Node> nodes = new LinkedBlockingQueue<>();

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean offer(Node node) {
        if (ips.add(node.getAddr().getHostString()))
            return nodes.offer(node);
        return false;
    }

    public Node poll() {
        Node node = nodes.poll();
        if (node != null)
            ips.remove(node.getAddr().getHostString());
        return node;
    }

    public Node take() throws InterruptedException {
        Node node = nodes.take();
        ips.remove(node.getAddr().getHostString());
        return node;
    }


}
