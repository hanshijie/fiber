package fiber.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import static fiber.io.Log.log;

public final class CSVParser {
	public final static class ParserException extends RuntimeException {
		private static final long serialVersionUID = -2868307092569353100L;
		
		private final String file;
		private final int line;
		private final int pos;
		private final String msg;
		public ParserException(String file, int line, int pos, String msg) {
			this.file = file;
			this.line = line;
			this.pos = pos;
			this.msg = msg;
		}
		@Override
		public String toString() { return String.format("file:{} line:{} pos:{}, err:{}", file, line, pos, msg); }
	}
	
	private final static String DEFAULT_CHARSET = "GB2312";
	
	private final String fileName;
	private final ArrayList<ArrayList<String>> datas;
	private int line;
	private int pos;
	
	public static CSVParser fromFile(String fileName, String charset) throws Exception {
		return new CSVParser(fileName, new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset)));
	}
	
	public static CSVParser fromFile(String fileName) throws Exception {
		return fromFile(fileName, DEFAULT_CHARSET);
	}
	
	public static CSVParser fromString(String content) throws Exception {
		return new CSVParser("#string", new StringReader(content));
	}
	
	public CSVParser(String fileName, Reader reader) throws Exception {
		this.fileName = fileName;
		this.datas = new ArrayList<ArrayList<String>>();
		for(CSVRecord record : CSVFormat.DEFAULT.parse(reader)) {
			ArrayList<String> line = new ArrayList<String>();
			for(String field : record) {
				line.add(field);
			}
			this.datas.add(line);
		};
		
		this.pos = 0;
		this.line = 0;
	}
	
	void error(int line, int pos, String msg) {
		throw new ParserException(this.fileName, line, pos, msg);
	}
	
	void error(String msg) {
		error(this.line, this.pos - 1, msg);
	}
	
	void error(boolean succ, String msg) {
		if(!succ) error(msg);
	}
	
	public String readString() {
		error(!isEnd(), "unmarshal fail");
		return this.datas.get(this.line).get(this.pos++);
	}
	
	public boolean readBoolean() {
		int v = readInteger();
		error(v == 0 || v == 1, String.format("<{}> is invalid bool", v));
		return v != 0;
	}
	
	public int readInteger() {
		error(!isEnd(), "unmarshal fail");
		String v = this.datas.get(this.line).get(this.pos++);
		int intv = Integer.parseInt(v);
		error(Integer.toString(intv).equals(v), String.format("<{}> is invalid int", v));
		return intv;
	}
	
	public long readLong() {
		error(!isEnd(), "unmarshal fail");
		String v = this.datas.get(this.line).get(this.pos++);
		long longv = Long.parseLong(v);
		error(Long.toString(longv).equals(v), String.format("<{}> is invalid long", v));
		return longv;	
	}
	
	public CSVParser getRecordParser() throws Exception {
		return fromString(readString().replace("|", ","));
	}
	
	public void skipline(int num) {
		if(num <= 0) return;
		this.line += num;
		this.pos = 0;
	}
	
	public void skiprow(int num) {
		if(num <= 0) return;
		this.pos += num;
	}
	
	/*
	public boolean isEOF() {
		return this.line >= this.datas.size() || (this.pos >= this.datas.get(this.line).size() && this.line + 1 == this.datas.size());
	}
	
	public boolean isEOC() {
		return isEOF() || cur().equals("<END>");
	}
	
	public boolean isEOR() {
		return isEOF();
	}
	*/
	public String cur() {
		return this.datas.get(this.line).get(this.pos);
	}
	
	public boolean isEnd() {
		return this.line >= this.datas.size() || this.pos >= this.datas.get(this.line).size() || cur().equals("<END>");
	}
	
	public void dump() {
		log.info("file:{} line:{} pos:{}", this.fileName, this.line, this.pos);
		int linenum = 0;
		for(ArrayList<String> line : this.datas) {
			String s = "";
			for(String field : line) {
				s = s + "<" + field + ">";
			}
			log.info("{}:{}", ++linenum, s);
		}
	}
	
	public static void main(String[] args) throws Exception {
		CSVParser p = fromFile("e:/achie_table.csv");
		p.dump();
	}
	
}
