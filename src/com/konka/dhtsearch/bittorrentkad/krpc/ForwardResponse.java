package com.konka.dhtsearch.bittorrentkad.krpc;

import java.util.List;

import com.konka.dhtsearch.Node;

/**
 * A forward response as defined in the colors protocol
 * TODO: add a link to the published article
 *
 */
public class ForwardResponse extends KadResponse {

	private static final long serialVersionUID = 6325079396969335098L;
	
	
	private List<Node> nodes = null;
	private boolean ack = false;
	private boolean nack = false;
	
	ForwardResponse(long id, Node src) {
		super(id, src);
	}

	public List<Node> getNodes() {
		return nodes;
	}
	
	
	public ForwardResponse setNodes(List<Node> nodes) {
		this.nodes = nodes;
		return this;
	}
	
	
	public ForwardResponse setAck() {
		if (isNack())
			throw new IllegalStateException("cannot be both ack and nack");
		this.ack = true;
		return this;
	}
	
	public ForwardResponse setNack() {
		if (isAck())
			throw new IllegalStateException("cannot be both ack and nack");
		this.nack = true;
		return this;
	}
	
	
	public boolean isAck() {
		return ack;
	}
	
	public boolean isNack() {
		return nack;
	}
	

}