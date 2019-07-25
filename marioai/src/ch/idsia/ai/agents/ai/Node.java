package ch.idsia.ai.agents.ai;

public class Node {
	
	int x, y;
	int sceneValue;
	float distanceTraveled;
	float heuristicDistance;
	Node parent = null;
	boolean isOnClosedList = false;
	boolean isOnOpenList = false;
	int openListPos;
	
	public Node(int sceneValue, int x, int y){
		this.sceneValue = sceneValue;
		this.x = x;
		this.y = y;
	}
	
	public float getCost(){
		return distanceTraveled + heuristicDistance;
	}
	
	//Getters and Setters
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	public int getSceneValue() {
		return sceneValue;
	}
	public void setSceneValue(int sceneValue) {
		this.sceneValue = sceneValue;
	}

	public float getDistanceTraveled() {
		return distanceTraveled;
	}
	public void setDistanceTraveled(float distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}
	public float getHeuristicDistance() {
		return heuristicDistance;
	}
	public void setHeuristicDistance(float heuristicDistance) {
		this.heuristicDistance = heuristicDistance;
	}
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public boolean isOnClosedList() {
		return isOnClosedList;
	}
	public void setOnClosedList(boolean isOnClosedList) {
		this.isOnClosedList = isOnClosedList;
	}
	public boolean isOnOpenList() {
		return isOnOpenList;
	}
	public void setOnOpenList(boolean isOnOpenList) {
		this.isOnOpenList = isOnOpenList;
	}
	public int getOpenListPos() {
		return openListPos;
	}
	public void setOpenListPos(int openListPos) {
		this.openListPos = openListPos;
	}	
	
}
