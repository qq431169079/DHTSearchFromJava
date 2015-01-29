package com.konka.dhtsearch.bittorrentkad;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import com.konka.dhtsearch.Key;
import com.konka.dhtsearch.KeyFactory;
import com.konka.dhtsearch.KeybasedRouting;
import com.konka.dhtsearch.Node;
import com.konka.dhtsearch.bittorrentkad.bucket.KadBuckets;
import com.konka.dhtsearch.bittorrentkad.handlers.FindNodeHandler;
import com.konka.dhtsearch.bittorrentkad.handlers.PingHandler;
import com.konka.dhtsearch.bittorrentkad.krpc.ContentMessage;
import com.konka.dhtsearch.bittorrentkad.krpc.get_peers.GetPeersRequest;
import com.konka.dhtsearch.bittorrentkad.net.KadServer;

public class KadNet implements KeybasedRouting {

	// dependencies
//	private final MessageDispatcher<Object> msgDispatcher;
	// private final JoinOperation joinOperation;// 加入指定的节点
	private final GetPeersRequest contentRequestProvider;
	private final ContentMessage contentMessage;
	private final KadBuckets findValueOperation;// 查找相识节点用
	private final FindNodeHandler findNodeHandler;
	private final PingHandler pingHandler;

	private final Node localNode;// 本地节点
	private final KadServer kadServer;// Runnable 主要是TODO KadServer
	private final NodeStorage nodeStorage;// 路由表
	private final KeyFactory keyFactory;// key生成器
	private final ExecutorService clientExecutor;// 线程池
	private final int bucketSize;// 一个k桶大小
	private final TimerTask refreshTask;// 定时器
	private final BootstrapNodesSaver bootstrapNodesSaver;// 关机后保存到本地，启动时候从本地文件中加载

	// state
	// private final Map<String, MessageDispatcher<?>> dispatcherFromTag = new
	// HashMap<String, MessageDispatcher<?>>();
	private Thread kadServerThread = null;

	public KadNet(GetPeersRequest contentRequestProvider, ContentMessage contentMessageProvider, KadBuckets findValueOperationProvider, //
			FindNodeHandler findNodeHandlerProvider, PingHandler pingHandler, //
			Node localNode, KadServer kadServer, NodeStorage nodeStorage, //
			KeyFactory keyFactory, ExecutorService clientExecutor, int bucketSize, TimerTask refreshTask,//
			BootstrapNodesSaver bootstrapNodesSaver) {

		// this.msgDispatcher = msgDispatcherProvider;
		// this.joinOperation = joinOperationProvider;
		this.contentRequestProvider = contentRequestProvider;
		this.contentMessage = contentMessageProvider;
		this.findValueOperation = findValueOperationProvider;
		this.findNodeHandler = findNodeHandlerProvider;
		this.pingHandler = pingHandler;
		// this.storeHandler = storeHandlerProvider;
		// this.forwardHandlerProvider = forwardHandlerProvider;

		this.localNode = localNode;
		this.kadServer = kadServer;
		this.nodeStorage = nodeStorage;
		this.keyFactory = keyFactory;
		this.clientExecutor = clientExecutor;
		this.bucketSize = bucketSize;
		this.refreshTask = refreshTask;
		this.bootstrapNodesSaver = bootstrapNodesSaver;

	}

	@Override
	public void create() throws IOException {
		// bind communicator and register all handlers
		// kadServer.bind();
		pingHandler.register();
		findNodeHandler.register();
		// storeHandler.register();
		// forwardHandlerProvider.register();

		nodeStorage.registerIncomingMessageHandler();
		kadServerThread = new Thread(kadServer);
		kadServerThread.start();

		bootstrapNodesSaver.load();
		bootstrapNodesSaver.start();
	}

	@Override
	public void join(Collection<URI> bootstraps) {
		// joinOperation.addBootstrap(bootstraps).doJoin();
	}

	@Override
	public List<Node> findNode(Key k) {// 根据k返回相似节点
		List<Node> result = findValueOperation.getClosestNodesByKey(k, 8);

		// List<Node> result = op.doFindValue();
		// findNodeHopsHistogram.add(op.getNrQueried());

		List<Node> $ = new ArrayList<Node>(result);

		if ($.size() > bucketSize)
			$.subList(bucketSize, $.size()).clear();

		// System.out.println(op.getNrQueried());

		return result;
	}

	@Override
	public KeyFactory getKeyFactory() {
		return keyFactory;
	}

	@Override
	public List<Node> getNeighbours() {
		return nodeStorage.getAllNodes();
	}

	@Override
	public Node getLocalNode() {
		return localNode;
	}

	@Override
	public String toString() {
		return localNode.toString() + "\n" + nodeStorage.toString();
	}

	@Override
	public void sendMessage(Node to, String tag, Serializable msg) throws IOException {
		kadServer.send(to, contentMessage.setTag(tag).setContent(msg));
	}

	@Override
	public void shutdown() {
		try {
			bootstrapNodesSaver.saveNow();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		refreshTask.cancel();
		kadServer.shutdown(kadServerThread);
	}
}
