package com.github.raptortechjs.LoveLetter.TwoPlayer.Players;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.Action;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.GameObserver;
import com.github.raptortechjs.LoveLetter.TwoPlayer.Game.GameState3;

public class HashingLogger implements GameObserver {
	//private List<Action> history = new ArrayList<>();
	private final MessageDigest m;
	
	public HashingLogger() {
		MessageDigest temp = null;
		try {
			temp = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		m = temp;
	}
	
	
	public void accept(Action action, GameState3 oldState, GameState3 newState) {
		//history.add(action);
		m.update(BigInteger.valueOf(action.normalize().hashCode()).toByteArray());
		
	}
	
	public String digest() {
		/*String plaintext = "your text here";
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(plaintext.getBytes());*/
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		return hashtext;
	}

}
