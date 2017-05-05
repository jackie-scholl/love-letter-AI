package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.HashSet;
import java.util.Set;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.ConsoleLogger;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.ConsolePlayer;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.HashingLogger;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.RandomPlayer;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("hello world");
		
		Set<String> hashes = new HashSet<>();
		
		long start = System.currentTimeMillis();
		int lastHashesSize = 0;
		for (int i=0; i<1e10; i++) {
			if (i % 1e6 == 1) {
				long end = System.currentTimeMillis();
				double differencePer = (end - start)*1.0/i;
				System.out.printf("%d; %.3e; %.3f%n", hashes.size(), differencePer, (hashes.size() - lastHashesSize)*1.0/1e6);
				lastHashesSize = hashes.size();
				//System.out.println(hashes.size());
			}
			hashes.add(getHash());
		}
		
		//GameState2 state = new GameState2.Builder().build();
	}
	
	public static String getHash() {
		HashingLogger h = new HashingLogger();
		//Game g = new Game(new ConsolePlayer(), new ConsolePlayer(), new ConsoleLogger(), h);
		Game g = new Game(new RandomPlayer(), new RandomPlayer(), h);
		g.runThrough();
		return h.digest();
	}

}
