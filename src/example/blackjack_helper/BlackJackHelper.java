package example.blackjack_helper;

/**
 Copyright (C) 2017  Zombie_Striker

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **/

import java.util.*;

//import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import example.blackjack_helper.QuickBlackJackHandler.Card;
import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputNumberNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Numbers;
import me.zombie_striker.neuralnetwork.util.DeepReinforcementUtil;

public class BlackJackHelper extends NNBaseEntity implements Controler {

	/**
	 * This bot will help you play blackjack. Given your hand, it will tell you if
	 * you should hit or stay.
	 */

	public Sensory2D_Numbers hand_view = new Sensory2D_Numbers(10, 15);

	public boolean wonTheGame = true;

	QuickBlackJackHandler game = new QuickBlackJackHandler();
	List<Card> hands;
	List<Card> house_hands;
	/**
	 * New decks are usefull for making sure the same arangement of cards is rare.
	 */
	boolean newDeck = true;

	public BlackJackHelper(boolean createAI) {
		super(false, 2000);

		if (createAI) {
			this.ai = NNAI.generateAI(this, 1, 5, "Should Stay");
			// Stay = 0, Hit = 1;
			for (int amountOfCardsInHand = 0; amountOfCardsInHand < 10; amountOfCardsInHand++) {
				for (int highestCardIndex = 11; highestCardIndex >= 2; highestCardIndex--) {
					// 1st is the amount of cards in the hand. 2nd is the value
					// of the card. 10s,j,q,k are all treated as tens. A's are
					// 11.
					InputNumberNeuron.generateNeuronStatically(ai, amountOfCardsInHand, highestCardIndex,
							this.hand_view);
				}
			}

			// Three layers of inputs for maximum pattern recognition
			for (int neurons = 0; neurons < 43; neurons++)
				Neuron.generateNeuronStatically(ai, 1);
			for (int neurons = 0; neurons < 25; neurons++)
				Neuron.generateNeuronStatically(ai, 2);
			for (int neurons = 0; neurons < 15; neurons++)
				Neuron.generateNeuronStatically(ai, 3);

			// Its best just to keep 1 bias neuron, though it normally
			// deactivates itself on its own.
			BiasNeuron.generateNeuronStatically(ai, 0);
			connectNeurons();
		}
		this.controler = this;
		this.setNeuronsPerRow(0, 10);
	}

	public String learn() {

		// TODO: Sort this out in the next update. This is just copied code. It should
		// work, but this needs to be refined.
		/**
		 * Since this method is far to big to comment on everything, here is the bacis
		 * of what is happening:
		 * 
		 * 1)Create a new deck if it needs to
		 * 
		 * 2)If training, give the NN and the houseAI two new cards.
		 * 
		 * 3) Tick and think.
		 * 
		 * 4) It is says hit, give it a new card. If it goes over 21, it loses the game.
		 * Else, it continues playing with a new card in its deck. There are no new
		 * decks if this happens
		 * 
		 * 5)Else, if it says to stay, check if it is equal to 21. If so, it wins.
		 * Else....
		 * 
		 * 6) Let the dealerAI play. It will pick cards up until it goes over 18, in
		 * which case it stays. Although it can still go over, stopping at 18 means it
		 * is less likely
		 * 
		 * 7) Check if the NN's cards are under 21, but higher than the house. If so, it
		 * wins. If not, check the next card it would have picked if it had hit.
		 * 
		 * 8) If that card is still under 21, noWin is set to false.
		 * 
		 * 9) After all that, check if it lost, and that there way a way to win (we
		 * don't want to punish the NN if it would have lost even if it hit). If this is
		 * true, use DeepReinforcement.
		 * 
		 * 10) Return the message.
		 */
		if (!shouldLearn)
			newDeck = true;
		if (newDeck) {
			if (shouldLearn) {
				int k = 0;
				while (k < 52) {
					k++;
					game.newDeck();
					hands = new ArrayList<QuickBlackJackHandler.Card>();
					hands.add(game.cardsInDeck[0]);
					hands.add(game.cardsInDeck[1]);

					house_hands = new ArrayList<QuickBlackJackHandler.Card>();
					house_hands.add(game.cardsInDeck[2]);
					house_hands.add(game.cardsInDeck[3]);
					game.currentIndex = 3;
					if (game.cardsInDeck[0].value + game.cardsInDeck[1].value <= 21
							&& game.cardsInDeck[2].value + game.cardsInDeck[3].value < 21)
						break;
				}
			} else {
				game.newDeck();
				hands = new ArrayList<QuickBlackJackHandler.Card>();
				house_hands = new ArrayList<QuickBlackJackHandler.Card>();
				for (int i = 0; i < 10; i++) {
					for (int col = 0; col <= 11; col++) {
						if (this.hand_view.getNumberAt(i, col) == 1) {
							hands.add(new Card(col, i));
						}
					}
				}
				/*
				 * for (int i = 0; i < 2; i++) { for (int col = 0; col <= 11; col++) { if
				 * (base.hand_view.getNumberAt(i + 10, col) == 1) { house_hands.add(new
				 * Card(col, i)); } } }
				 */
				// DO NOT SHOW HOUSE
			}
		}
		if (shouldLearn) {
			int[] allCards = new int[hands.size()];
			int[] allCardsH = new int[house_hands.size()];
			List<Integer> values = new ArrayList<>();
			List<Integer> valuesH = new ArrayList<>();
			for (int i = 0; i < hands.size(); i++)
				allCards[i] = hands.get(i).value;
			for (int i = 0; i < house_hands.size(); i++)
				allCardsH[i] = house_hands.get(i).value;

			/**
			 * Sorting by size does increase the accuracy by 1-2% (Given a 2000 entry set),
			 * however this does not actually teach the NN how to add, just which neurons it
			 * should look for if it should stay/hit. Considering the small percentage
			 * increase and the dishonesty of what is going on, this has been commented out.
			 * However, if you would like to test it, feel free to remove the marks.
			 */
			/*
			 * Arrays.sort(allCards); Arrays.sort(allCardsH); ArrayUtils.reverse(allCards);
			 * ArrayUtils.reverse(allCardsH);
			 */
			// TODO: Do not sort by size

			for (int i = 0; i < allCards.length; i++)
				values.add(allCards[i]);
			for (int i = 0; i < allCardsH.length; i++)
				valuesH.add(allCardsH[i]);

			for (int i = 0; i < 10; i++) {
				for (int col = 0; col <= 11; col++) {
					boolean isValue = false;
					if (values.size() > i && values.get(i) == col) {
						isValue = true;
					}
					this.hand_view.changeNumberAt(i, col, isValue ? 1 : 0);
				}
			}
		}

		boolean[] thought = tickAndThink();
		boolean shouldStay = thought[0];

		float accuracy = 0;

		if (!shouldLearn) {
			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A").replaceAll("12", "J").replaceAll("13", "Q")
						.replaceAll("14", "K") + ",");
			}
			newDeck = true;
			return (((thought.length > 2 ? thought[2] : false) ? ChatColor.GOLD
					: shouldStay ? ChatColor.BLUE : ChatColor.WHITE) + "You should:" + ((shouldStay ? "Stay" : "Hit"))
					+ "|= " + sb.toString());

		} else {

			String reasonForOutcome = "";

			boolean noWin = false;
			boolean continuePlaying = false;
			// Action logic
			if (!shouldStay) {
				if (hands.size() <= 10) {
					// Hit
					int totalHand = 0;
					for (Card c : hands)
						totalHand += c.value;

					Card cc = game.cardsInDeck[++game.currentIndex];
					hands.add(cc);
					totalHand += cc.value;

					if (totalHand >= 21) {
						wonTheGame = (totalHand == 21);
						if (wonTheGame) {
							continuePlaying = true;
							reasonForOutcome = "Hit;got 21";
						} else {
							reasonForOutcome = "Hit;over 21";
						}
					} else {
						continuePlaying = true;
						reasonForOutcome = "Hit; good enough";
						wonTheGame = true;
					}
				} else {
					wonTheGame = false;
					reasonForOutcome = "Hit; Over 10 cards.";
				}
			} else {

				// Stay
				int totalHand = 0;
				for (Card c : hands)
					totalHand += c.value;

				if (totalHand > 21) {
					wonTheGame = false;
					reasonForOutcome = ChatColor.DARK_AQUA + "How the hell did this happen!?!?";
				} else if (totalHand == 21) {
					wonTheGame = true;
					reasonForOutcome = "Stayed; got 21";
				} else {

					// The Dealer's AI
					int totalHouseHand = 0;
					for (Card ch : house_hands)
						totalHouseHand += ch.value;

					// Dealer draw
					for (int i = 2; i < 10; i++) {
						if (game.cardsInDeck[game.currentIndex - 1 + (i - 2)].value + totalHouseHand >= 18) {
							break;
						}
						house_hands.add(game.cardsInDeck[++game.currentIndex]);
						totalHouseHand += house_hands.get(i).value;
					}

					if (totalHouseHand > 21) {
						wonTheGame = true;
						reasonForOutcome = "Stayed; dealer went over 21";
					} else if (totalHand > totalHouseHand) {
						// The NN is closer to 21
						wonTheGame = true;
						reasonForOutcome = "Stayed; closer to 21";
					} else {
						wonTheGame = false;
						if (house_hands.size() > 2 ? (totalHand + house_hands.get(2).value <= 21)
								: (totalHand + game.cardsInDeck[game.currentIndex + 1].value <= 21)) {
							reasonForOutcome = "Stayed; Should have hit";
						} else {
							// Even if the NN drew another card, they would not
							// have won
							noWin = true;
							reasonForOutcome = "Stayed; No-Win";
						}
					}
				}
			}
			// If end of game, add result
			// TODO: EDIT testing: even if the game continues, mark it as solved
			// as long as they CAN continue.
			// if (!continuePlaying)
			if (!noWin || wonTheGame)
				this.getAccuracy().addEntry(wonTheGame);
			accuracy = (float) this.getAccuracy().getAccuracy();

			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A").replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "k") + ",");
			}
			StringBuilder sb2 = new StringBuilder();
			for (Card c : house_hands) {
				sb2.append((c.number + "").replaceAll("11", "A").replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "k") + ",");
			}

			boolean lowChanceForSuccess = false;

			// Quick hacky way to see the chance for correct card before the
			// hit
			Card potentialHitCard = null;
			int k = 0;
			if (!shouldStay) {
				potentialHitCard = hands.get(hands.size() - 1);
				hands.remove(potentialHitCard);
				k = 1;
			}

			int totalHand = 0, totalHHand = 0;
			for (Card c : hands)
				totalHand += c.value;

			for (int i = 0; i < 2; i++)
				totalHHand += house_hands.get(i).value;

			double chanceForRightCard = 0;

			// +2 for the two in the houses hand
			for (int i = hands.size() + 2 - k; i < 52; i++) {
				if (totalHand + game.cardsInDeck[i].value <= 21 && totalHand + game.cardsInDeck[i].value > totalHHand)
					chanceForRightCard++;
			}

			chanceForRightCard /= (52 - 2 - hands.size() + k);
			if (chanceForRightCard < 0.5)
				lowChanceForSuccess = true;
			// Re-add the card
			if (potentialHitCard != null)
				hands.add(potentialHitCard);

			// IMPROVE IT
			HashMap<Neuron, Double> map = new HashMap<>();

			// If staying wins or if hitting loses, set shouldStay to should
			// stay (+1). Else, hit (-1)
			if (shouldStay == wonTheGame) {
				map.put(ai.getNeuronFromId(0), 1.0);
			} else {
				map.put(ai.getNeuronFromId(0), -1.0);
			}

			// If you did not win, and there was a way to win.
			if (!wonTheGame && !noWin)
				DeepReinforcementUtil.instantaneousReinforce(this, map, 1);

			// If game ended, ask for new deck
			if (!continuePlaying) {
				newDeck = true;
			} else {
				newDeck = false;
			}

			return (((int) (100 * accuracy)) + "|"
					+ (lowChanceForSuccess != shouldStay
							? ChatColor.GOLD + "[LOW-"
									+ ((int) (100 * (shouldStay ? 1 - chanceForRightCard : chanceForRightCard))) + "]"
							: "")
					+ (noWin ? ChatColor.YELLOW + ""
							: continuePlaying ? ChatColor.GRAY
									: (wonTheGame ? ChatColor.GREEN + "" : ChatColor.RED + ""))
					+ "| " + reasonForOutcome + "|= " + sb + "/" + sb2 + "|");

		}
	}

	@Override
	public String update() {
		/**
		 * Since this method is far to big to comment on everything, here is the bacis
		 * of what is happening:
		 * 
		 * 1)Create a new deck if it needs to
		 * 
		 * 2)If training, give the NN and the houseAI two new cards.
		 * 
		 * 3) Tick and think.
		 * 
		 * 4) It is says hit, give it a new card. If it goes over 21, it loses the game.
		 * Else, it continues playing with a new card in its deck. There are no new
		 * decks if this happens
		 * 
		 * 5)Else, if it says to stay, check if it is equal to 21. If so, it wins.
		 * Else....
		 * 
		 * 6) Let the dealerAI play. It will pick cards up until it goes over 18, in
		 * which case it stays. Although it can still go over, stopping at 18 means it
		 * is less likely
		 * 
		 * 7) Check if the NN's cards are under 21, but higher than the house. If so, it
		 * wins. If not, check the next card it would have picked if it had hit.
		 * 
		 * 8) If that card is still under 21, noWin is set to false.
		 * 
		 * 9) After all that, check if it lost, and that there way a way to win (we
		 * don't want to punish the NN if it would have lost even if it hit). If this is
		 * true, use DeepReinforcement.
		 * 
		 * 10) Return the message.
		 */
		if (!shouldLearn)
			newDeck = true;
		if (newDeck) {
			if (shouldLearn) {
				int k = 0;
				while (k < 52) {
					k++;
					game.newDeck();
					hands = new ArrayList<QuickBlackJackHandler.Card>();
					hands.add(game.cardsInDeck[0]);
					hands.add(game.cardsInDeck[1]);

					house_hands = new ArrayList<QuickBlackJackHandler.Card>();
					house_hands.add(game.cardsInDeck[2]);
					house_hands.add(game.cardsInDeck[3]);
					game.currentIndex = 3;
					if (game.cardsInDeck[0].value + game.cardsInDeck[1].value <= 21
							&& game.cardsInDeck[2].value + game.cardsInDeck[3].value < 21)
						break;
				}
			} else {
				game.newDeck();
				hands = new ArrayList<QuickBlackJackHandler.Card>();
				house_hands = new ArrayList<QuickBlackJackHandler.Card>();
				for (int i = 0; i < 10; i++) {
					for (int col = 0; col <= 11; col++) {
						if (this.hand_view.getNumberAt(i, col) == 1) {
							hands.add(new Card(col, i));
						}
					}
				}
				/*
				 * for (int i = 0; i < 2; i++) { for (int col = 0; col <= 11; col++) { if
				 * (base.hand_view.getNumberAt(i + 10, col) == 1) { house_hands.add(new
				 * Card(col, i)); } } }
				 */
				// DO NOT SHOW HOUSE
			}
		}
		if (shouldLearn) {
			int[] allCards = new int[hands.size()];
			int[] allCardsH = new int[house_hands.size()];
			List<Integer> values = new ArrayList<>();
			List<Integer> valuesH = new ArrayList<>();
			for (int i = 0; i < hands.size(); i++)
				allCards[i] = hands.get(i).value;
			for (int i = 0; i < house_hands.size(); i++)
				allCardsH[i] = house_hands.get(i).value;

			/**
			 * Sorting by size does increase the accuracy by 1-2% (Given a 2000 entry set),
			 * however this does not actually teach the NN how to add, just which neurons it
			 * should look for if it should stay/hit. Considering the small percentage
			 * increase and the dishonesty of what is going on, this has been commented out.
			 * However, if you would like to test it, feel free to remove the marks.
			 */
			/*
			 * Arrays.sort(allCards); Arrays.sort(allCardsH); ArrayUtils.reverse(allCards);
			 * ArrayUtils.reverse(allCardsH);
			 */
			// TODO: Do not sort by size

			for (int i = 0; i < allCards.length; i++)
				values.add(allCards[i]);
			for (int i = 0; i < allCardsH.length; i++)
				valuesH.add(allCardsH[i]);

			for (int i = 0; i < 10; i++) {
				for (int col = 0; col <= 11; col++) {
					boolean isValue = false;
					if (values.size() > i && values.get(i) == col) {
						isValue = true;
					}
					this.hand_view.changeNumberAt(i, col, isValue ? 1 : 0);
				}
			}
		}

		boolean[] thought = tickAndThink();
		boolean shouldStay = thought[0];

		float accuracy = 0;

		if (!shouldLearn) {
			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A").replaceAll("12", "J").replaceAll("13", "Q")
						.replaceAll("14", "K") + ",");
			}
			newDeck = true;
			return (((thought.length > 2 ? thought[2] : false) ? ChatColor.GOLD
					: shouldStay ? ChatColor.BLUE : ChatColor.WHITE) + "You should:" + ((shouldStay ? "Stay" : "Hit"))
					+ "|= " + sb.toString());

		} else {

			String reasonForOutcome = "";

			boolean noWin = false;
			boolean continuePlaying = false;
			// Action logic
			if (!shouldStay) {
				if (hands.size() <= 10) {
					// Hit
					int totalHand = 0;
					for (Card c : hands)
						totalHand += c.value;

					Card cc = game.cardsInDeck[++game.currentIndex];
					hands.add(cc);
					totalHand += cc.value;

					if (totalHand >= 21) {
						wonTheGame = (totalHand == 21);
						if (wonTheGame) {
							continuePlaying = true;
							reasonForOutcome = "Hit;got 21";
						} else {
							reasonForOutcome = "Hit;over 21";
						}
					} else {
						continuePlaying = true;
						reasonForOutcome = "Hit; good enough";
						wonTheGame = true;
					}
				} else {
					wonTheGame = false;
					reasonForOutcome = "Hit; Over 10 cards.";
				}
			} else {

				// Stay
				int totalHand = 0;
				for (Card c : hands)
					totalHand += c.value;

				if (totalHand > 21) {
					wonTheGame = false;
					reasonForOutcome = ChatColor.DARK_AQUA + "How the hell did this happen!?!?";
				} else if (totalHand == 21) {
					wonTheGame = true;
					reasonForOutcome = "Stayed; got 21";
				} else {

					// The Dealer's AI
					int totalHouseHand = 0;
					for (Card ch : house_hands)
						totalHouseHand += ch.value;

					// Dealer draw
					for (int i = 2; i < 10; i++) {
						if (game.cardsInDeck[game.currentIndex - 1 + (i - 2)].value + totalHouseHand >= 18) {
							break;
						}
						house_hands.add(game.cardsInDeck[++game.currentIndex]);
						totalHouseHand += house_hands.get(i).value;
					}

					if (totalHouseHand > 21) {
						wonTheGame = true;
						reasonForOutcome = "Stayed; dealer went over 21";
					} else if (totalHand > totalHouseHand) {
						// The NN is closer to 21
						wonTheGame = true;
						reasonForOutcome = "Stayed; closer to 21";
					} else {
						wonTheGame = false;
						if (house_hands.size() > 2 ? (totalHand + house_hands.get(2).value <= 21)
								: (totalHand + game.cardsInDeck[game.currentIndex + 1].value <= 21)) {
							reasonForOutcome = "Stayed; Should have hit";
						} else {
							// Even if the NN drew another card, they would not
							// have won
							noWin = true;
							reasonForOutcome = "Stayed; No-Win";
						}
					}
				}
			}
			// If end of game, add result
			// TODO: EDIT testing: even if the game continues, mark it as solved
			// as long as they CAN continue.
			// if (!continuePlaying)
			if (!noWin || wonTheGame)
				this.getAccuracy().addEntry(wonTheGame);
			accuracy = (float) this.getAccuracy().getAccuracy();

			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A").replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "k") + ",");
			}
			StringBuilder sb2 = new StringBuilder();
			for (Card c : house_hands) {
				sb2.append((c.number + "").replaceAll("11", "A").replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "k") + ",");
			}

			boolean lowChanceForSuccess = false;

			// Quick hacky way to see the chance for correct card before the
			// hit
			Card potentialHitCard = null;
			int k = 0;
			if (!shouldStay) {
				potentialHitCard = hands.get(hands.size() - 1);
				hands.remove(potentialHitCard);
				k = 1;
			}

			int totalHand = 0, totalHHand = 0;
			for (Card c : hands)
				totalHand += c.value;

			for (int i = 0; i < 2; i++)
				totalHHand += house_hands.get(i).value;

			double chanceForRightCard = 0;

			// +2 for the two in the houses hand
			for (int i = hands.size() + 2 - k; i < 52; i++) {
				if (totalHand + game.cardsInDeck[i].value <= 21 && totalHand + game.cardsInDeck[i].value > totalHHand)
					chanceForRightCard++;
			}

			chanceForRightCard /= (52 - 2 - hands.size() + k);
			if (chanceForRightCard < 0.5)
				lowChanceForSuccess = true;
			// Re-add the card
			if (potentialHitCard != null)
				hands.add(potentialHitCard);

			// IMPROVE IT
			HashMap<Neuron, Double> map = new HashMap<>();

			// If staying wins or if hitting loses, set shouldStay to should
			// stay (+1). Else, hit (-1)
			if (shouldStay == wonTheGame) {
				map.put(ai.getNeuronFromId(0), 1.0);
			} else {
				map.put(ai.getNeuronFromId(0), -1.0);
			}

			// If you did not win, and there was a way to win.
			if (!wonTheGame && !noWin)
				DeepReinforcementUtil.instantaneousReinforce(this, map, 1);

			// If game ended, ask for new deck
			if (!continuePlaying) {
				newDeck = true;
			} else {
				newDeck = false;
			}

			return (((int) (100 * accuracy)) + "|"
					+ (lowChanceForSuccess != shouldStay
							? ChatColor.GOLD + "[LOW-"
									+ ((int) (100 * (shouldStay ? 1 - chanceForRightCard : chanceForRightCard))) + "]"
							: "")
					+ (noWin ? ChatColor.YELLOW + ""
							: continuePlaying ? ChatColor.GRAY
									: (wonTheGame ? ChatColor.GREEN + "" : ChatColor.RED + ""))
					+ "| " + reasonForOutcome + "|= " + sb + "/" + sb2 + "|");

		}

	}

	@Override
	public NNBaseEntity clone() {
		BlackJackHelper clone = new BlackJackHelper(false);
		clone.ai = this.ai.clone(clone);
		return clone;
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				sb.append(args[i].replaceAll("10", "j"));
			}
			String cards = sb.toString();// args[1].replaceAll("10", "j");
			int[] cardValues = new int[cards.length()];
			for (int i = 0; i < cards.length(); i++) {
				cardValues[i] = cards.charAt(i) == 'j' || cards.charAt(i) == 'k' || cards.charAt(i) == 'q' ? 10
						: cards.charAt(i) == 'a' ? 11 : Integer.parseInt(cards.charAt(i) + "");
			}
			for (int in = 0; in < this.hand_view.getMatrix().length; in++) {
				for (int in2 = 0; in2 < this.hand_view.getMatrix()[in].length; in2++) {
					this.hand_view.changeNumberAt(in, in2, cardValues.length > in && cardValues[in] == in2 ? 1 : 0);
				}
			}

		} else {
			initiator.sendMessage("Provide a hand (10's should be written as j)");
		}

	}

	@Override
	public void setBase(NNBaseEntity base) {
		// This is empty because there is no need to set the base. However,
		// since some people may want to have the controler in a seperate class,
		// this method is still needed
	}

}
