import java.io.Serializable;

public class Message implements Serializable {
	
	private int xUID;
	private int distance;
	private int round;
	private int degree;
	private String msgType;			//"SEARCH" / "POSITIVEACK" / "NEGATIVEACK"
	
	public Message(){
		this.round = 0;
		this.distance = 0;
	}

	public int getxUID() {
		return xUID;
	}

	public void setxUID(int xUID) {
		this.xUID = xUID;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	@Override
	public String toString() {
		return "Message [xUID=" + xUID + ", distance=" + distance + ", round=" + round + ", degree=" + degree
				+ ", msgType=" + msgType + "]";
	}

}
