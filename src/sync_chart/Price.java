package sync_chart;

public class Price {
	private double close;
	private double bid;
	private double ask;
	private long acceptTime;

	public Price(double close, double bid, double ask, long acceptTime) {
		this.close = close;
		this.bid = bid;
		this.ask = ask;
		this.acceptTime = acceptTime;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
	}
	public double getAsk() {
		return ask;
	}
	public void setAsk(double ask) {
		this.ask = ask;
	}
	public long getAcceptTime() {
		return acceptTime;
	}
	public void setAcceptTime(long acceptTime) {
		this.acceptTime = acceptTime;
	}
	@Override
	public String toString() {
		return "Price [close=" + close + ", bid=" + bid + ", ask=" + ask + ", acceptTime=" + acceptTime + "]";
	}
}
