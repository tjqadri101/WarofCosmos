package WarOfCosmos;

import java.util.concurrent.TimeUnit;

import jgame.*;
import jgame.platform.*;

public class WOC extends StdGame {

	private int numEnemy;
	private int numCollis;
	private int bcount;
	private int health;
	private double bossX;
	private double bossY;
	private int level2;
	private int gun;
	
	public static void main(String[]args) {new WOC(new JGPoint(640,480));}
	public WOC() { initEngineApplet(); }
	public WOC(JGPoint size) { initEngine(size.x,size.y); }
	public void initCanvas() { setCanvasSettings(128,96,8,8,null,null,null); }
	
	public void initGame() {
		setFrameRate(35,2);
		defineMedia("woc.tbl");
		setBGImage("myback");
		try { Thread.sleep(1000); }
		catch (InterruptedException e) {}
		setHighscores(10,new Highscore(0,"nobody"), 25);
		startgame_ingame=true;
		initial_lives = 5;
	}
	
	public void initNewLife() {
		removeObjects(null,0);
		bcount = 0;
		new Player(pfWidth()/2,pfHeight()-64,5);
	}
	
	
	/** Called when a new level is started. */
	public void defineLevel() {
		removeObjects(null,0);
		new Player(pfWidth()/2,pfHeight()-64,5);
	}

	public void startGameOver() { removeObjects(null,0); }
	public void doFrameInGame() {
		moveObjects();
		checkCollision(2,1); // enemies hit player
		checkCollision(2,4);
		checkCollision(4,2); // bullets hit enemies
		//checkCollision(4,8);
		//checkCollision(8,4);
		if(level == 0){
			if (checkTime(0,(int)(800),
				(int)((30 + (int)(Math.random() * ((50 ) + 1))))))
					new Enemy();
			if (gametime>=800 && countObjects("enemy",0)==0 || numEnemy >= 7){
				levelDone();
			}
				
		}
		if(level == 1){
			if(bcount == 0){
				bcount = 1;
				new Boss();
			}
			if(numCollis >= 10){
				new JGObject("burn",true,bossX,bossY,0,"burn", 0,0, -1);
				levelDone();
			}
		}
		if(level >= 2){
			numEnemy = 0;
			numCollis = 0;
			bcount = 0;
			health = 0;
			level2 = 0;
			
			gun = 0;
			gameOver();
		}
		if (getKey('N')) {
			levelDone();
		}
		if (getKey('S')) {
			lifeLost();
		}
	}
	public void incrementLevel() {
		score += 50;
		lives = 5;
		if(level == 1){
			level++;
		}
		if(level < 1){
			level2 = 1;
			level++;
		}
		
	}
	JGFont scoring_font = new JGFont("Arial",0,8);
	
	public void paintFrameInGame() {
		setFont(new JGFont("arial",0,15));
		drawString("Press N for the next level, or S to lose a life.",
			pfWidth()/2,180,0);
		if(level == 0){
			drawString("Destroy 7 enemies to move on", pfWidth()/2,50, 0);
			drawString("Or survive for some time to complete the level",
					pfWidth()/2, 100, 0);
		}
		if(level == 1){
			health = numCollis*10;
			int curHealth = 100 - health;
			String a = Integer.toString(curHealth);
			String bossHealth = "Boss Life Left: " + a + "%";
			drawString(bossHealth, pfWidth()/2, 100, 0);
			
		}
		if(gun >= 15){
			drawString("RayShot unlocked. Use key_fireup. Score bonus on every hit!",
					pfWidth()/2, 50, 0);
		}
	}
	public void paintFrameLevelDone(){
		setFont(new JGFont("arial",0,18));
		drawString("Level done", pfWidth()/2, pfHeight()/2, 0);
		if(level2 == 1){
			
			drawString("You Won!", pfWidth()/2, 50, 0);
		}
	}
	public class Enemy extends JGObject {
		double timer=0;
		public Enemy() {
			super("enemy",true,random(32,pfWidth()-40),0,
					2, "enemy",
					random(-1,1), 3, -2 );
			xspeed = random(-2,2);
		}
		public void move() {
			timer += gamespeed;
			if (checkTime(0,(int)(800),
				(int)((30 + (int)(Math.random() * ((50) + 1))))) && countObjects("ray",0) < 2){
				new Ray(x, y);
			}
			x += Math.sin(0.1*timer);
			y += Math.cos(0.1*timer);
			if (y>pfHeight()) y = -8;
				
		}
		public void hit(JGObject o) {
			remove();
			o.remove();
			score += 10;
			numEnemy +=1;
		}
	}

	public class Player extends JGObject {
		public Player(double x,double y,double speed) {
			super("player",true,x,y,1,"player", 0,0,speed,speed,-1);
		}
		public void move() {
			setDir(0,0);
			if (getKey(key_left)  && x > xspeed)               xdir=-1;
			if (getKey(key_right) && x < pfWidth()-32-yspeed)  xdir=1;
			if(getKey(key_up) && y>0) ydir = -1;
			if(getKey(key_down)&& y<pfHeight()-64) ydir = 1;
			if(getKey(key_fireleft) && countObjects("left", 0) < 2){
				new Left(x,y);
				clearKey(key_fireleft);
			}
			if(getKey(key_fireright) && countObjects("right", 0) < 2){
				new Right(x,y);
				clearKey(key_fireright);
			}
			if (getKey(key_fire) && countObjects("bullet",0) < 2) {
				new Bullet(x, y);
				clearKey(key_fire);
			}
			if(gun >= 15){
				if(getKey(key_fireup) && countObjects("rshot", 0) < 2){
					new Rshot(x,y);
					clearKey(key_fireup);
				}
			}
		}
		public void hit(JGObject obj) {
			if (and(obj.colid,2)) lifeLost();
			else {
				score += 5;
				obj.remove();
			}
		}
	}
	public class Bullet extends JGObject {
		public Bullet(double x,double y) {
			super("bullet",true,x,y,4,"red", 0, -5, -2);
		}
	}
	
	public class Left extends JGObject {
		public Left(double x,double y) {
			super("left",true,x,y,4,"left", -5, 0, -2);
		}
	}
	public class Right extends JGObject {
		public Right(double x,double y) {
			super("right",true,x,y,4,"right", 5, 0, -2);
		}
	}
	public class Ray extends JGObject {
		public Ray(double x,double y) {
			super("ray",true,x,y,2,"green", 0, 5, -2);
		}
		public void hit(JGObject o) {
			remove();
			o.remove();
			score += 5;
		}
	}

	public class Boss extends JGObject {
		public Boss() {
			super("boss",true,pfWidth()/2,pfHeight()/2,
					2, "boss",
					2, 2, -1 );
		}
	
		public void move() {
				
			if ( (x >=  pfWidth()-200 && xspeed>0)
			||   (x <=            200  && xspeed<0) ) {
						xspeed = -xspeed;
						
			}
			if ( (y >= pfHeight()-200 && yspeed>0)
			||   (y <=            200 && yspeed<0) ) {
						yspeed = -yspeed;
						
			}
			
			if (checkTime(0,(int)(20000),
					(int)((10 + (int)(Math.random() * ((30) + 1))))) && countObjects("meteor",0) < 2){
					new Meteor(x, y);
					gun++;
				}
			if (checkTime(0,(int)(20000),
					(int)((40 + (int)(Math.random() * ((60) + 1))))) && countObjects("ufo",0) < 2){
					new Saucer(x, y);
					gun += 2;
				}	
		}	
		
		
		public void hit(JGObject o){
				o.remove();
				score += 15;
				numCollis++;
			if(numCollis >= 10){
				bossX = x;
				bossY = y;
				remove();
			}
			
		}
	}
	public class Meteor extends JGObject {
		public Meteor(double x,double y) {
			super("meteor",true,x,y,2,"meteor", 0, 5, -2);
		}
		public void hit(JGObject o) {
			remove();
			o.remove();
			score += 5;
		}
	}
	public class Saucer extends JGObject {
		double timer=0;
		public Saucer(double x, double y) {
			super("ufo",true,x,y,
					2, "ufo",
					random(-1,1), (1.0+level/2.0), -2 );
			xspeed = random(-2,2);
			yspeed = random(-2,2);
		}
		public void move() {
			timer += gamespeed;
			if (checkTime(0,(int)(2000),
				(int)((50 + (int)(Math.random() * ((90) + 1))))) && countObjects("ray",0) < 2){
				new Gshot(x, y);
			}
			x += Math.sin(0.1*timer);
			y += Math.cos(0.1*timer);
			if (y>pfHeight()) y = -8;
				
		}
		public void hit(JGObject o) {
			remove();
			o.remove();
			score += 20;
		}
	}
	public class Gshot extends JGObject {
		public Gshot(double x,double y) {
			super("gshot",true,x,y,2,"gshot", random(-2,2), random(-2,2), -2);
		}
		public void hit(JGObject o) {
			remove();
			o.remove();
			score += 5;
		}
	}
	public class Rshot extends JGObject {
		public Rshot(double x,double y) {
			super("rshot",true,x,y,4,"rshot", 0, -5, -2);
		}
		public void hit(JGObject o){
			score += 10;
		}
	}	
}

