package com.konka.dhtsearch.parser;

import com.konka.dhtsearch.db.mongodb.MongoCollection;
import com.konka.dhtsearch.util.Util;

/**
 * MultiFile类，用于定义一个多文件的结构，根据BT协议规范，多文件结果里每一个文件的结构是：
 * lenght 整数, 文件大小 
 * md5sum, 字符串， 可选 
 * path 路径，文件夹和文件名的 bencode 编码的列表 
 * 
 */
@MongoCollection
public class MultiFile {
	private long singleFileLength;     //定义多文件里每个文件的长度
	private String path;               //定义path

	/*
     * 以下是这些属性的getXXX和setXXX的方法，用于存取各个属性值
     */
	public long getSingleFileLength() {
		return singleFileLength;
	}
	public void setSingleFileLength(long singleFileLength) {
		this.singleFileLength = singleFileLength;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getFormatSize(){
		return Util.getFormatSize(singleFileLength);
	}
}
