package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.utils.MathX;

import java.util.*;

public class HybridAgent extends BasicAIAgent implements Agent
{
	final int RIGHT = 0;
	final int LEFT = 1;
	final int JUMP = 2;
	final int DOWN = 3;
	
    float[] enemiesPos;
    float[] marioPos;
    int marioSize;
	
	//Timers
	int timer = 0;
	int imminentDangerRightTimer = 0;
	int imminentDangerLeftTimer = 0;
	int gapJumpTimer = 0;
	int obstaclesInFrontTimer = 0;
	int obstacleBehindTimer = 0;
	int enemyInPathTimer = 0;
	int stuckTimer = 0;
	int plantTimer;
	
	//Anti stuck
	float maxRight = 0;
	float minLeft = 10000;
	boolean stuckChange = false;
	boolean isStuck = false;
	
	//AStar
	int aStarTimer = 0;
	boolean aStarActive = false;
	int instructionPathCounter = 0;
	int[] instructionPath;
	int lastTargetX = 0;
	int lastTargetY = 1;
	int aStarType = 0;
	boolean aStarLeft = false;
	
	//Other bools
	boolean killNearEnemy = true;
	
	boolean actionTaken = false;
	
	
	//Movement booleans	
    int trueJumpCounter = 0;
    boolean jumping = false;
    
    int trueSpeedCounter = 0;

    int permaBoostRandCounter = 0;
    boolean permaBoost = false;
    
    int boostCounter = 0;
    boolean boosting = false;
    
    boolean jumpBoosting = false;
    
    int rightPausedCounter = 0;
   // int timeSinceLastRightPause = 0;
    boolean rightPaused = false;
    
    int movingLeftCounter = 0;
    boolean movingLeft = false;
    
    private Random rand = null;
    
    public HybridAgent()
    {
        super("HybridAgent");
        reset();
    }

    public void reset()
    {
    	rand  = new Random();
    	
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
        trueJumpCounter = 0;
        trueSpeedCounter = 0;
        
        timer = 0;
    	imminentDangerRightTimer = 0;
    	 imminentDangerLeftTimer = 0;
    	 gapJumpTimer = 0;
    	 obstaclesInFrontTimer = 0;
    	 obstacleBehindTimer = 0;
    	 enemyInPathTimer = 0;
    	 stuckTimer = 0;
    	
    	//Anti stuck
    	maxRight = 0;
    	minLeft = 10000;
    	stuckChange = false;
    	
    	
    	//AStar
    	 instructionPathCounter = 0;
    	 lastTargetX = 0;
    	 lastTargetY = 1;
    	
    	//Other bools
    	killNearEnemy = true;
    	
    	actionTaken = false;
    	
    	
    	//Movement booleans	
         trueJumpCounter = 0;
        jumping = false;
        
         trueSpeedCounter = 0;

         permaBoostRandCounter = 0;
        permaBoost = false;
        
         boostCounter = 0;
        boosting = false;
        
        jumpBoosting = false;
        
         rightPausedCounter = 0;
       //  timeSinceLastRightPause = 0;
        rightPaused = false;
        
         movingLeftCounter = 0;
    }

    private boolean DangerOfGap(byte[][] completeScene)
    {
        for (int x = 9; x < 13; ++x)
        {
            boolean f = true;
            for(int y = 12; y < 22; ++y)
            {
                if  (completeScene[y][x] != 0)
                    f = false;
            }
            if (f && completeScene[12][11] != 0)
                return true;
        }
        return false;
    }
    
    private boolean DangerOfGapBehind(byte[][] completeScene)
    {
        for (int x = 9; x < 13; ++x)
        {
            boolean f = true;
            for(int y = 12; y < 22; ++y)
            {
                if  (completeScene[y][x] != 0)
                    f = false;
            }
            if (f && completeScene[12][11] != 0)
                return true;
        }
        return false;
    }

    private boolean noEnemiesOnScreen(byte[][] enemyScene){
    	for(int i = 9; i < 22; i++){
    		for(int j = 0; j < 22; j++){
    			if(enemyScene[j][i] !=0 && enemyScene[j][i] != 25){
   				//System.out.println("First enemy at x:" + i + " y: " + j);
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    private byte[][] decode(String estate)
    {
        byte[][] dstate = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        for (int i = 0; i < dstate.length; ++i)
            for (int j = 0; j < dstate[0].length; ++j)
                dstate[i][j] = 2;
        int row = 0;
        int col = 0;
        int totalBitsDecoded = 0;

        for (int i = 0; i < estate.length(); ++i)
        {
            char cur_char = estate.charAt(i);
            if (cur_char != 0)
            {
                //MathX.show(cur_char);
            }
            for (int j = 0; j < 16; ++j)
            {
                totalBitsDecoded++;
                if (col > Environment.HalfObsHeight*2 - 1)
                {
                    ++row;
                    col = 0;
                }

//                if ((MathX.pow(2,j) & cur_char) != 0)
                if ((MathX.powsof2[j] & cur_char) != 0)
                {

                    try{
                        dstate[row][col] = 1;
//                        show(cur_char);
                    }
                    catch (Exception e)
                    {
                        //System.out.println("row = " + row);
                        //System.out.println("col = " + col);
                    }
                }
                else
                {
                    dstate[row][col] = 0; //TODO: Simplify in one line of code.
                }
                ++col;
                if (totalBitsDecoded == 484)
                    break;
            }
        }

        //System.out.println("totalBitsDecoded = " + totalBitsDecoded);
        return dstate;
    }


    public boolean[] getAction(Environment observation)
    {
        //TODO: Discuss increasing diffuculty for handling the gaps.
        // this Agent requires observation.

    	++timer;
    
    	marioSize = observation.getMarioMode();
    	
        assert(observation != null);
        byte[][] completeScene = observation.getCompleteObservation(/*1, 0*/);
        byte[][] levelScene = observation.getLevelSceneObservation();
        byte[][] enemyScene = observation.getEnemiesObservation();
        //byte[][] enemyScene = observation.getEnemiesObservationZ(1);
        marioPos = observation.getMarioFloatPos();
        enemiesPos = observation.getEnemiesFloatPos();
        if(enemiesPos.length > 0){
        	//System.out.println("Enemy pos: [" + enemiesPos[0] + "][" + enemiesPos[1] + "]");
        }
        boolean imminentThreatReal = true;
        
        //System.out.println("Mario's X Pos: " + marioPos[0]);
        if(stuck(marioPos)){
        	aStarActive = false;
        	aStarTimer = 0;
        	isStuck = true;
        	System.out.println("STUCK: Mario is stuck.  Reset...");
        	reset();
        	//new Scanner(System.in).nextLine();
        }

        if(isStuck){
        	++aStarTimer;
        	if(aStarTimer > 24){
        		isStuck = false;
        		aStarTimer = 0;
        	}
        }
        
 
        
       // System.out.println("RightPaused: " + rightPaused);
       // System.out.println("RightPausedCounter: " + rightPausedCounter);
        
       // System.out.println("THIS SHOULD BE A BRICK: " + completeScene[12][11]);
       // System.out.println("THIS SHOULD BE EMPTY: " + completeScene[10][11]);
        
        //System.out.println("Direct INSIDE of Mario: " + levelScene[11][11]);
       // System.out.println("Direct RIGHT of Mario: " + levelScene[11][12]);
       // System.out.println("Direct LEFT of Mario: " + levelScene[11][10]);
        
        //Reset jump counter
        if((jumping && observation.isMarioOnGround())){
        	System.out.println("Resetting jump button");
        	action[Mario.KEY_JUMP] = false;
        	jumping = false;
        	return action;
        }
        
        //Manage AStar
        if(aStarActive){
            trueJumpCounter = 0;
            jumping = false;
            
            trueSpeedCounter = 0;

            permaBoostRandCounter = 0;
            permaBoost = false;
            
            boostCounter = 0;
            boosting = false;
            
            jumpBoosting = false;
            
            rightPausedCounter = 0;
            
            movingLeftCounter = 0;
            movingLeft = false;
            
            
        	if(instructionPathCounter >= instructionPath.length){
        		//System.out.println("AStar DEACTIVATED");
        		aStarActive = false;
        		instructionPathCounter = 0;
        	}
        	
        }
        
        //Manage jump counter
        if (trueJumpCounter > 16){	
            jumping = false;
            trueJumpCounter = 0;
        }
        
        //Manage perma boost
        /*
    	if(noEnemiesOnScreen(enemyScene)){
    		//System.out.println("No Enemies on screen");
    		//No threats on screen, so sprint!
        	permaBoost = true;
        	permaBoostRandCounter = 0;
    	} else{
    		permaBoost = false;
    		/*
    		//System.out.println("Enemies are on screen");
        	if(permaBoostRandCounter > 16){
        		permaBoost = false;
        		permaBoostRandCounter = 0;
        	} else {
        		permaBoost = true;
        		++permaBoostRandCounter;
        	}
        	*/
    	
    	
        
        //ManageJumpBoost
        if(jumpBoosting){
        	if(observation.isMarioOnGround()){
        		jumpBoosting = false;
        	}
        
        //Manage boost counter
        }else if(boostCounter > 15){
        	//System.out.println("Sprinting OFF");
        	boosting = false;
        }
        
        //Manage right pause counter
        if(rightPausedCounter > 15){
        	rightPaused = false;
        }
        
        //Manage left movement counter
        if(movingLeftCounter > 15){
        	movingLeft = false;
        }
        
        
        //Piranha plant
 
        //Anti stuck stuff
        ///////////////////////////
        //Check if Koopa shell
        /////////////////////////////
        
        //Check if enemies above mario
        //////////////////////////////
        boolean enemyPlantNear = false;
        for(int i = -5; i <= 5; i++){
        	for(int j = 0; j < 5; j++){
        		if(enemyScene[11 + i][12 + j] == 12){
        			enemyPlantNear = true;
        			//System.out.println("Piranha Plant Near");
        		}
        	}
        }
        
    	
        //Perform plant actions
	    if(enemiesPos.length > 0 && enemyPlantNear && !isStuck){
	    	boolean enemyPlantVeryNear = false;
	        for(int i = -5; i <= 5; i++){
	        	for(int j = 0; j < 1; j++){
	        		if(enemyScene[11 + i][12 + j] == 12){
	        			enemyPlantVeryNear = true;
	        			System.out.println("Piranha Plant Very Near");
	        		}
	        	}
	        }
	        
	       // System.out.printf("Mario Y: %.0f  Plant Y: %.0f \n", marioPos[1], enemiesPos[2]);
	        
	        if(enemyPlantVeryNear){
	        	stopMovingRight();
	        	plantTimer ++;
	        	if(marioPos[1] <= enemiesPos[2] + 30 || plantTimer > 48){
	        		plantTimer = 0;
		    		jumping = true;
		    		rightPaused = false;
		    	} else {
		    		//stopMovingRight();
		    		/*
		    		System.out.println("EVASION: Piranha Plant Danger; Evasive Action (Right side)");
		        	stopMovingRight();
		        	startMovingBoostLeft();
		        	*/
		    	}
	        }
	    	
	    }
    	
        //Check if enemies present immediate danger to mario moving right
	    else if(!rightPaused && imminentRightDanger(enemyScene)){// || enemyScene[11][14] != 0)){

        	if(timer - imminentDangerRightTimer > 15){
        		killNearEnemy = true;
        		if(rand.nextFloat() < 0.4){//0.3){
		        	//Boost to left.  Once enemy is far enough mario will reset to normal jump protocol.
		        	System.out.println("EVASION: Imminent Enemy Danger; Evasive Action (Right side)");
		        	stopMovingRight();
		        	startMovingBoostLeft();
		        	killNearEnemy = false;
        		}
        		
        		if(killNearEnemy){
            		System.out.println("FORWARD: Killing very close enemy RIGHT");
            		//Jump over/on enemy
            		performJump(observation);
            	}
        		
        		imminentDangerRightTimer = timer;
        	}
        	
        
        //Check if enemies present immediate danger to mario moving left
        } else if((movingLeft || aStarLeft) && imminentLeftDanger(enemyScene)){//|| enemyScene[11][10] != 0)){
        	if(timer - imminentDangerLeftTimer > 15){
        		killNearEnemy = true;
	        	if(rand.nextFloat() < 0.4){//0.3){
		    		//System.out.println("ENEMY is: " + enemyScene[11][12] + " or " + enemyScene[11][13]);
		        	//Boost to left.  Once enemy is far enough mario will reset to normal jump protocol.
		        	System.out.println("EVASION: Imminent Enemy Danger; Evasive Action (Left side)");
		        	movingLeft = true;
		        	rightPaused = false;
		        	killNearEnemy = false;
	            }
	        	
	        	if(killNearEnemy){
	        		System.out.println("FORWARD: Killing very close enemy LEFT");
	        		//Jump over/on enemy
	        		performJump(observation);
	        	}
	        	imminentDangerLeftTimer = timer;
            }
            	
        //Check if enemy in path of mario
        } else if(!rightPaused && enemiesInPath(enemyScene)){
        	//System.out.println("Enemy in path");
        	if(timer - enemyInPathTimer > 15){
        		actionTaken = false;
	        	//Slow down near enemy
	        	if(rand.nextFloat() < 0.05){
	        		System.out.println("HESITATING: Slow down around enemy");
	        		permaBoost = false;
	    
	        		actionTaken = true;
	        	}
	        	
	        	//Hesitate around enemy
	        	if(rand.nextFloat() < 0.1){
	        		System.out.println("HESITATING: Hesitating around enemy");
	        		//Pause near enemy
	        		stopMovingRight();
	        		
	        		//Hesitate around enemy
	        		startMovingLeft();
	        		
	        		actionTaken = true;
	        	}
	        	
	        	if(!actionTaken){
		        	//Jump over/on enemy
	        		System.out.println("FORWARD: Jumping on/over enemy ahead");
		    		performJump(observation);
	        	}	
	        	
        		enemyInPathTimer = timer;
        	}

        	
        	
        //Check far gaps while sprinting (Jump over gaps differently if sprinting)
        } else if(permaBoost && !rightPaused && (levelScene[11][15] != 0 || levelScene[11][16] != 0 || DangerOfGap(completeScene)) ){
    		//System.out.println("Sprinting while jumping over obstacle");
        	//System.out.println("NAVIGATION: Gap to right while SPRINTING - JUMPING");
        	performJump(observation);
        	
        	
       //Check if gap in front of mario (For normal walking speed)
        } else if(!rightPaused && DangerOfGap(completeScene)){
        	if(timer - gapJumpTimer > 5){
        		actionTaken = false;
	        	//Hesitate on jump over a gap
	        	if(rand.nextFloat() < 0.1){//0.05){
	        		System.out.println("HESITATING: Hesitating over gap jump");
	        		stopMovingRight();
	        		startMovingLeft();
	        		actionTaken = true;
	        	}
	        	if(!actionTaken){
	        		//System.out.println("NAVIGATION: Gap to right - JUMPING");
		        	jumpBoost();
		    		performJump(observation);
	        	}
	        	
	        	gapJumpTimer = timer;
        	}
        	
  
        //Check if obstacle in front of mario
        }else if(!rightPaused && (levelScene[11][13] != 0 || levelScene[11][12] != 0)){
	 		//System.out.println("NAVIGATION: Obstacle to right - JUMPING");
	 		//System.out.println("Permaboost: " + permaBoost);
	 		performJump(observation);
	 		
	 		
        //Jump over obstacle behind mario if moving left
        } else if((movingLeft || aStarLeft) && (levelScene[11][9] != 0 || levelScene[11][10] != 0 || levelScene[11][11] != 0 || DangerOfGap(completeScene))){
        	//System.out.println("NAVIGATION: Obstacle to left - JUMPING");
        	performJump(observation);
        	

            //AStar Navigating;
        } else if(aStarActive){
    	    	
			//System.out.println("AStar ACTIVE");
	    	boolean actionTaken = false;
	    	if(instructionPath.length > 1 && !actionTaken){
	    		if(!(marioSize !=0 && aStarType == 34)){
		    		if(instructionPath[1] == JUMP && instructionPath[0] == RIGHT){
			    		//System.out.println("ASTAR: Jumping");
			    		rightPaused = false;
			    		jumping = true;
			    		actionTaken = true;
		    		} else if(instructionPath[1] == JUMP && instructionPath[0] == LEFT){
			    		//System.out.println("ASTAR: Jumping");
			    		stopMovingRight();
			    		startMovingLeft();
			    		aStarLeft= true;
			    		jumping = true;
			    		actionTaken = true;
			    	}
	    		}
		    }
	    	 
	    	if(instructionPath.length > 0 && !actionTaken){
			    if(instructionPath[0] == RIGHT){
		    		//System.out.println("ASTAR: Moving Right");
		    		rightPaused = false;
		    		actionTaken = true;
			    		
		    	} else if(instructionPath[0] == LEFT){
		    		//System.out.println("ASTAR: Moving Left");
		    		stopMovingRight();
		    		startMovingLeft();
		    		aStarLeft = true;
		    		actionTaken = true;
		    	} else if(instructionPath[0] == JUMP){
		    		if(!(marioSize !=0 && aStarType == 34 && instructionPath.length == 1)){
			    		//System.out.println("ASTAR: Jumping from directly Underneath");
			    		jumping = true;
			    		actionTaken = true;
		    		}
		    	}
	    	}
    	    	
    	    	++instructionPathCounter;
    	    	
        //No gaps or obstacles.  Stopping jump.  Checking for collectibles
        } else {
        	//	jumping = false;
        	
        	if(!isStuck){
        		aStarActive = performAStar(observation.getLevelSceneObservationZ(1), observation.getEnemiesObservationZ(2));
        	}
        	
        	//Boost
        	if(noEnemiesOnScreen(enemyScene)){
        		//System.out.println("No Enemies on screen");
        		//No threats on screen, so sprint!
            	permaBoost = true;
            	permaBoostRandCounter = 0;
        	} else{
        		permaBoost = false;
        	}
        	
        	
	            	
        	
        }
        
	    if(!aStarActive){
	    	aStarLeft = false;
	    }
	    
        //Manage Jump key
        if(jumping){
        	action[Mario.KEY_JUMP] = true;
        	++trueJumpCounter;
        } else {
        	action[Mario.KEY_JUMP] = false;
        }
        
        //Manage boost key
        if(boosting || jumpBoosting){
        	action[Mario.KEY_SPEED] = true;
        	++boostCounter;
        } else{
        	action[Mario.KEY_SPEED] = false;
        }
        
        //Manage permaboost
        if(permaBoost){
        	action[Mario.KEY_SPEED] = true;
        } else{
        	action[Mario.KEY_SPEED] = false;
        }
        
        //Manage right key
    	if(rightPaused){
    		action[Mario.KEY_RIGHT] = false;
        	++rightPausedCounter;
    	} else {
    		action[Mario.KEY_RIGHT] = true;
    	}
    	
    	//Manage left key
    	if(movingLeft){
        	action[Mario.KEY_LEFT] = true;
        	++movingLeftCounter;
    	} else {
        	action[Mario.KEY_LEFT] = false;
    	}

       // action[Mario.KEY_SPEED] = DangerOfGap(completeScene);
    	//System.out.println("Directly under target is: " + observation.getLevelSceneObservationZ(1)[lastTargetY + 1][lastTargetX]);
        return action;
    }
    
    boolean performAStar(byte[][] scene, byte[][] powerUpScene){
    	//System.out.println("Searching for AStar target...");
    	int targetL = -1;
    	int targetR = -1;
    	boolean targetFound = false;
    	
    	//Top down - Left Right
    	for(int j = 5; j < 17; j++){
    		for(int i = 5; i < 17; i++){
    			
    			//Check for coins
    			if(scene[j][i] == 34 && (scene[j-1][i] != 34 && scene[j+1][i] != 34) && scene[j-1][i] != -11 ){
    				targetFound = true;
    				targetL = j;
    				targetR = i;
    				break;
    			}
    			
    			
    			//Check for Question Mark Blocks
    			if(scene[j][i] == 21){
    				targetFound = true;
    				targetL = j;
    				targetR = i;
    				break;
    			}		
    			
    			
    		}
    		if(targetFound){
    			break;
    		}
    	}
    	
    	boolean powerUpFound = false;
    	
    	//Top down - Left Right
    	for(int j = 0; j < 22; j++){
    		for(int i = 0; i < 22; i++){
    			
    			//Check for Mushrooms/Flowers
    			if(powerUpScene[j][i] == 14){// || powerUpScene[j][i] == 15){
    				powerUpFound = true;
    				targetL = j;
    				targetR = i;
    				break;
    			}
    			
    		}
    		if(powerUpFound){
    			break;
    		}
    	}
    	
    	if(targetFound || powerUpFound){
    		int marioY = 11;
    		if(marioSize > 0){
    			//marioY = 10;
    		} else{
    			marioY = 11;
    		}
    		
    		/*
    		System.out.println("Coins at: ");
    		for(int i = 5; i < 17; i++){
        		for(int j = 5; j < 17; j++){
        			if(scene[j][i] == 123){
        				System.out.print("[" + j + "][" + i + "] ");
        			}
        		}
        	}
    		System.out.println();
    		*/
    		
    		
    		//System.out.println("AStar Target " + scene[targetL][targetR] + " Found at: " + targetL + ", " + targetR);
    
    		aStarType = scene[targetL][targetR];
    		AStar search = new AStar(marioY, 11, targetL, targetR, scene, aStarType, marioSize);
    		instructionPath = search.computePath();
    		if(instructionPath.length > 0 && instructionPath[0] != -1){
    			
    			/*
    			System.out.println("Mario Mode: " + marioSize);
	        	System.out.println("Instruction Path: ");
	        	for(int i = 0; i < instructionPath.length; i++){
	        		System.out.print("[" + instructionPath[i] + "] ");
	        	}
	        	System.out.println();
	        	
	    		new Scanner(System.in).nextLine();
	        	*/
	        	
    			lastTargetX = targetR;
    			lastTargetY = targetL;
    			return true;
    		} else {
    			System.out.println("AStar cannot find path to target");
    		}

    	}
    	
    	return false;
    }
    
    boolean stuck(float[] pos){
    	if(pos[0] > maxRight){
    		//System.out.println("STUCKMGR: Mario reached new MAXRight");
    		maxRight = pos[0] + 10;
    		stuckChange = true;
    	}
    	/*
    	if(pos[0] < minLeft){
    		System.out.println("STUCKMGR: Mario reached new MINLeft");
    		minLeft = pos[0];
    		stuckChange = true;
    	}
    	*/
    	++stuckTimer;
    	
    	//System.out.println("STUCKMGR <Min, Max> : <" + minLeft + "><" + maxRight + ">");
    	
    	if(stuckTimer > 72){
    		System.out.println("STUCKMGR: TIME TO CHECK");
    		stuckTimer = 0;
    		if(!stuckChange){
    			return true;
    		}
    		
    		stuckChange = false;
    	}
    	return false;
    }
    
    boolean imminentThreatReal(byte[][] enemyScene, int l, int r){
	    //Check if imminent enemy is a real threat
	    //Check if enemy plant
    	/*
	    if(enemiesPos.length > 0){
	    	if(marioPos[1] > enemiesPos[1] ){
	    		return false;
	    	} else {
	    		return true;
	    	}
	    }
	    */
    	

    	
	    //Check if Mario's Fireball
	    if(enemyScene[l][r] == 25){
	    	return false;
	    }
	    
	    return true;
    }
    
    boolean imminentRightDanger(byte[][] enemyScene){
    	for(int i = -1; i <= 1; i++){
	    	if(imminentThreatReal(enemyScene, 11+i, 13) && enemyScene[11+i][13] != 0){
	    		return true;
	    	}
	    	if(imminentThreatReal(enemyScene, 11+i, 12) && enemyScene[11+i][12] != 0){
	    		return true;
	    	}
    	}
    	return false;
    }
    
    boolean imminentLeftDanger(byte[][] enemyScene){
    	for(int i = -1; i <= 1; i++){
	    	if(imminentThreatReal(enemyScene, 11+i, 9) && enemyScene[11+i][9] != 0){
	    		return true;
	    	}
	    	if(imminentThreatReal(enemyScene, 11+i, 10) && enemyScene[11+i][10] != 0){
	    		return true;
	    	}
    	}
    	return false;
    }
    
    boolean enemiesInPath(byte[][] enemyScene){
    	for(int i = -1; i <= 1; i++){
	    	if(imminentThreatReal(enemyScene, 11+i, 14) && enemyScene[11 + i][14] != 0){
	    		return true;	
	    	}
	    	if(imminentThreatReal(enemyScene, 11+i, 15) && enemyScene[11 + i][15] != 0){
	    		return true;
	    	}
    	}
    	return false;
    }
 
    void stopMovingRight(){
    	//System.out.println("MOVEMENT: Right Stopped");
    	rightPaused = true;
		rightPausedCounter = 0;
    }
    
    void startMovingLeft(){
    	//System.out.println("MOVEMENT: Left Begun");
    	movingLeft = true;
		movingLeftCounter = 0;
    }
    
    void startMovingRight(){
    	movingLeft = false;
		movingLeftCounter = 15;
    }
    
    void startMovingBoostLeft(){
    	//System.out.println("MOVEMENT: Sprinting On");
    	startMovingLeft();
    	boosting = true;
    	boostCounter = 0;
    }
    
    void jumpBoost(){
    	jumpBoosting = true;
    }
    
    void performJump(Environment observation){
        if (observation.mayMarioJump() || ( !observation.isMarioOnGround() && action[Mario.KEY_JUMP]))
        {
            jumping = true;
        } else{
        	//System.out.println("CAN'T JUMP - NEED TO!");
        }	
    }
    
}
