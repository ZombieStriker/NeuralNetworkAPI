package example.blackjack_helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class QuickBlackJackHandler {

	/**
	 * This is  a small class for handling the decks. You don't really need to see this to understand what is happening.
	 */
	
	
	public Card[] cardsInDeck = new Card[52];
	public int currentIndex = 0;
	
	public QuickBlackJackHandler() {
		newDeck();
	}
	
	//26 :45 or 26/71 ~~ 37%
	//Human trials
	
	public void newDeck(){
		currentIndex=0;
		List<Integer> usedSlots = new ArrayList<>();
		List<Card> originDeck = Card.newOrderedDeck();
		cardsInDeck = new Card[52];
		Random r = ThreadLocalRandom.current();
		
		//Shuffle
		for(int i = 0; i < originDeck.size();i++){
			int newGoodIndex = r.nextInt(cardsInDeck.length);
			while(true){
			if(usedSlots.contains(newGoodIndex)){
				newGoodIndex=(newGoodIndex+1)%originDeck.size();
			}else{
				usedSlots.add(newGoodIndex);
				cardsInDeck[newGoodIndex] = originDeck.get(i);
				break;
			}
			}
		}
	}
	
	
	
	public static class Card{
		int number;
		int value;
		int type;
		public Card(int number, int type) {
			this.number = number;
			this.value = number>11?10:number;
			this.type = type;
		}
		public static List<Card> newOrderedDeck(){
			Card[] c = new Card[52];
			for(int type = 0; type < 4; type++){
				for(int number = 2; number < 15; number++){
					c[(type*13)+(number-2)]=new Card(number,type);
				}
			}
			List<Card> cc = new ArrayList<>();
			for(int i = 0; i < c.length;i++){
				cc.add(c[i]);
			}
			return cc;
		}
	}
}
