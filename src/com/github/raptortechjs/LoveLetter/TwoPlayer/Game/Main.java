package com.github.raptortechjs.LoveLetter.TwoPlayer.Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Players.*;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("hello world");
		
		Game g = new Game(new ConsolePlayer(), new ConsolePlayer(), new ConsoleLogger());
		//g.runThrough();
		
		for (int i=0; i<100; i++) {
			long start = System.currentTimeMillis();
			
			List<Double> scores = new ArrayList<>();
			
			for (int j=0; j < 1000; j++) {
				FullGameState3 s = FullGameState3.createNewGame();
				//System.out.println(s.deckSize());
				double score = Expectiminimaxer.score(s, /*i*/4);
				scores.add(score);
			}
			double average = scores.stream().mapToDouble(x -> x).average().getAsDouble();
			long end = System.currentTimeMillis();
			double diff = (end-start)/1000.0;
			System.out.printf("%d: %.2f; %.3f seconds%n", i, average, diff);
		}
		
		
		/*Set<String> hashes = new HashSet<>();
		
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
		}*/
		
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
