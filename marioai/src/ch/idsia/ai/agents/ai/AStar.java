package ch.idsia.ai.agents.ai;

import java.util.*;

public class AStar {
	final static int MAX_NEIGHBORS = 8;
	final static int MAX_GRID_X = 22;
	final static int MAX_GRID_Y = 22;
	
	byte[][] scene;
	
	int marioX;
	int marioY;
	int marioSize;
	int goalX;
	int goalY;
	int goalType;
	
	float tentHScore = 0;
	float tentGScore = 0;
	Node tentParent = null;
	
	ArrayList<Node> openList;
	
	Node[][] board;
	
	public AStar(int startL, int startR, int goalL, int goalR, byte[][] scene, int goalType, int marioSize){
		this.marioX = startR;
		this.marioY = startL;
		this.marioSize = marioSize;
		this.goalX = goalR;
		this.goalY = goalL;
		this.goalType = goalType;
		
		board = new Node[MAX_GRID_X][MAX_GRID_Y];
		openList = new ArrayList<Node>();
		this.scene = scene;
		
		for(int i = 0; i < MAX_GRID_X; i++){
			for(int j = 0; j < MAX_GRID_Y; j++){
				board[i][j] = new Node(scene[j][i], i , j);
			}
		}
	}

	
	public int[] computePath(){
		int curX = marioX;
		int curY = marioY;
		float curCost;
		Node parent;
		
		parent = board[curX][curY];
		curCost = computeCost(parent, goalX, goalY);
		parent.setDistanceTraveled(tentGScore);
		parent.setHeuristicDistance(tentHScore);
		openList.add(parent);
		parent.setOnOpenList(true);
		parent.setOpenListPos(0);
		
		//System.out.println("Goal is: " + goalX + ", " + goalY);
		
		while(!openList.isEmpty()){
			/*
			System.out.println("Open List: ");
	    	for(int i = 0; i < openList.size(); i++){
	    		System.out.print("[" + openList.get(i).getX() + "][" + openList.get(i).getY() + "]");
	    		System.out.print("(Distance Traveled: " + openList.get(i).getDistanceTraveled() + " Heuristic: " + openList.get(i).getHeuristicDistance() + ") ");
	    	}
	    	
	    	new Scanner(System.in).nextLine();
	    	*/
	    	
	    	
			int openListIndex = 0;
			curCost = openList.get(openListIndex).getCost();
			//Pop cheapest node off list
		
			
			//System.out.println("Popping cheapest node off list");
			for(int i = 0; i < openList.size(); i++){
				if(curCost > openList.get(i).getCost()){
					openListIndex = i;
					curCost  = openList.get(i).getCost();
				}
			}

			
			parent = openList.get(openListIndex);
			curX = parent.getX();
			curY = parent.getY();
			curCost = parent.getCost();
			
			openList.remove(openListIndex);
			parent.setOnOpenList(false);
			
			
			/*
			System.out.println("Popped: [" + curX + "][" + curY + "]");
			System.out.println();
			*/
			
			
			//Check if goal node
			if(curX == goalX && curY == goalY){
				/*
				System.out.println("Goal neighbors for " + goalX + ", " + goalY + " is: ");
				final int offsetX[] = {0, 1, 0, -1};
				final int offsetY[] = {-1, 0, 1, 0};
				for(int i = 0; i < 4; i++){
					int testY = goalY + offsetY[i];
					int testX = goalX + offsetX[i];
				System.out.print("Neighbor val [" + testX + "][" + testY + "] is " + scene[testY][testX] + " / ");
				}
				System.out.println();
				new Scanner(System.in).nextLine();
				*/
				
				return buildPath(parent);
			}

			//Look at neighbors
			tentParent = parent;
			final int offsetX[] = {0, 1, 0, -1};
			final int offsetY[] = {-1, 0, 1, 0};
			for(int i = 0; i < 4; i++){
				//Check if valid child node
				int childX = tentParent.getX() + offsetX[i];
				int childY = tentParent.getY() + offsetY[i];
				if(childX >= MAX_GRID_X || childY >= MAX_GRID_Y){
					continue;
				}
				if((childX == curX && childY == curY) || !validChildNode(childX, childY, curX, curY)){
					//System.out.println("Node Invalid: [" + childX + "][" + childY + "]");
					continue;
				}
				
				//Compute child cost
				Node childNode = board[childX][childY];
				boolean onClosedList = childNode.isOnClosedList();
				boolean onOpenList = childNode.isOnOpenList();
				int openListPos = childNode.getOpenListPos();
				float childCost = computeCost(childNode, goalX, goalY);
				
				if(!onClosedList && !onOpenList){
					openList.add(childNode);
					childNode.setOpenListPos(openList.size() - 1);
					childNode.setOnOpenList(true);
					childNode.setParent(tentParent);
					childNode.setDistanceTraveled(tentGScore);
					childNode.setHeuristicDistance(tentHScore);
					
				} else if(onOpenList && childCost < childNode.getCost()){
					//Update new cheaper node on open list
					childNode.setParent(tentParent);
					childNode.setDistanceTraveled(tentGScore);
					childNode.setHeuristicDistance(tentHScore);
				}
			}
			
			//Place parent on closed list
			//System.out.println("Placing parent on closed list.");
			parent.setOnClosedList(true);		
		}
		

		return falsePath();
	}
	
	private float computeCost(Node curNode, int endX, int endY){
		float hScore = 0;
		float gScore = 0;
		
		int xS = curNode.getX();
		int yS = curNode.getY();
		
		int xDiff;
		int yDiff;
		
		
		//calc G Score
		if(tentParent != null){
			/*
			if(marioSize > 0 && goalType == 34){
				final int offsetX[] = {0, 2, 0, -2};
				final int offsetY[] = {-2, 0, 2, 0};
				//System.out.println("Neighbors for " + "[" + xS + "][" + yS + "]: ");
				for(int h = 0; h < 4; h++){
			    	int testX = xS + offsetX[h];
			    	int testY = yS + offsetY[h];
			    	
					//System.out.print("[" + testX + "][" + testY + "]" + " is " + scene[testY][testX] + " ");
	
					if(testY < MAX_GRID_Y-1 && testX < MAX_GRID_X-1 && testY > 0 && testX > 0){
						if(scene[testY][testX] == -10){
							gScore = tentParent.getDistanceTraveled() + 1;
						}
					}
				}
			} else{
				*/
			final int offsetX[] = {0, 1, 0, -1};
			final int offsetY[] = {-1, 0, 1, 0};
			//System.out.println("Neighbors for " + "[" + xS + "][" + yS + "]: ");
			for(int h = 0; h < 4; h++){
		    	int testX = xS + offsetX[h];
		    	int testY = yS + offsetY[h];
		    	
				//System.out.print("[" + testX + "][" + testY + "]" + " is " + scene[testY][testX] + " ");

				if(testY < MAX_GRID_Y-1 && testX < MAX_GRID_X-1 && testY > 0 && testX > 0){
					if(scene[testY][testX] == -10){
						gScore = tentParent.getDistanceTraveled() + 1;
					}
				}
			}
			
				
			if(gScore == 0){
				gScore = tentParent.getDistanceTraveled() + 100;
			}
		}
		
		tentGScore = gScore;
		
		/////////////////////////////////////////
		////////HSCORE TIME/////////////////////
		/////////////////////////////////////////
		
		//Chebyshev
		yDiff = Math.abs(endY - yS);
		xDiff = Math.abs(endX - xS);
		hScore = Math.max(xDiff, yDiff);
		
		tentHScore = hScore;
		
		//Calc fscore
		return tentHScore + tentGScore;
		
	}
	
	private boolean validChildNode(int x, int y, int parX, int parY){
		if(x > MAX_GRID_X || y > MAX_GRID_Y){
			//Out of Bounds
			return false;
		} else if(x < 0 || y < 0){
			//Out of Bounds
			return false;
		} else if(x == parX && y == parY){
			//Child node is parent node
			return false;
		} else if(scene[y][x] == 16){
			//Node is a brick
			return false;
		} else if(scene[y][x] != 0 && scene[y][x] != goalType){
			//Node has no enemy/obstacle/wall
			return false;
		} else {
			if(goalType == 21 && (x == goalX && y == goalY) && y > parY){
				//Coming to brick from top instead of from bottom
				return false;
			}
			return true;
		}
		
	}
	
	private int[] buildPath(Node goalNode){
		final int RIGHT = 0;
		final int LEFT = 1;
		final int JUMP = 2;
		final int DOWN = 3;
		
		Node curNode = goalNode;
		ArrayList<Node> reversedPath = new ArrayList<Node>();
		Node[] correctPath;
		int[] instructionPath;
		
		reversedPath.add(curNode);
		while(curNode.getParent() != null){
			curNode = curNode.getParent();
			reversedPath.add(curNode);
		}
		
		//Reverse the path
		correctPath = new Node[reversedPath.size()];
		for(int i = 0; i < reversedPath.size(); i++){
			correctPath[i] = reversedPath.get(reversedPath.size() - 1 - i);
		}
		
		
		/*
		System.out.println("Final Path: ");
    	for(int i = 0; i < correctPath.length; i++){
    		System.out.print("[" + correctPath[i].getX() + "][" + correctPath[i].getY() + "] ");
    	}
    	System.out.println();
    	
    	//new Scanner(System.in).nextLine();
    	*/
		
		Node fromPos;
		Node toPos;
		instructionPath = new int[reversedPath.size() - 1];
		for(int i = 0; i < reversedPath.size()-1; i++){
			fromPos = correctPath[i];
			toPos = correctPath[i+1];
			if(fromPos.getX() == toPos.getX()){
				//Both on same X
				if(fromPos.getY() > toPos.getY()){
					//Drop down to target needed
					instructionPath[i] = JUMP;
				} else{
					//Jump needed
					instructionPath[i] = DOWN;
				}
			} else{
				//Both on same yumn
				if(fromPos.getX() > toPos.getX()){
					//Right move needed
					instructionPath[i] = LEFT;
				} else {
					//Left move needed
					instructionPath[i] = RIGHT;
				}
			}
		}
		return instructionPath;
		
	}
	
	private int[] falsePath(){
		int falsePath[] = {-1};
		return falsePath;
	}
	
}
