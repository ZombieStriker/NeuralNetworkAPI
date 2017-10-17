package example.blackjack_helper;

import java.util.*;

import org.apache.commons.lang.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import example.blackjack_helper.QuickBlackJackHandler.Card;
import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputNumberNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Numbers;
import me.zombie_striker.neuralnetwork.util.DeepReinformentUtil;

public class BlackJackHelper extends NNBaseEntity implements Controler {

	public Sensory2D_Numbers hand_view = new Sensory2D_Numbers(10, 15);

	public boolean wonTheGame = true;

	QuickBlackJackHandler game = new QuickBlackJackHandler();
	List<Card> hands;
	List<Card> house_hands;

	boolean newDeck = true;

	public BlackJackHelper(boolean createAI) {
		super(false);
		// this.senses.add(hand_view);
		if (createAI) {
			this.ai = NNAI.generateAI(this, 1, 3, "Should Stay");
			// Stay = 0, Hit = 1;
			for (int amountOfCardsInHand = 0; amountOfCardsInHand < 10; amountOfCardsInHand++) {
				for (int highestCardIndex = 11; highestCardIndex >= 2; highestCardIndex--) {
					// 1st is the amount of cards in the hand. 2nd is the value
					// of the card
					InputNumberNeuron.generateNeuronStatically(ai,
							amountOfCardsInHand, highestCardIndex,
							this.hand_view);
				}
			}

			// Creates the neurons for layer 1.
			for (int neurons = 0; neurons < 27; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);
			connectNeurons();
		}
		this.controler = this;
		this.setNeuronsPerRow(0, 11);
	}

	@Override
	public String update() {
		if (!shouldLearn) {
			newDeck = true;
		}
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
							&& game.cardsInDeck[2].value
									+ game.cardsInDeck[3].value < 21)
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
				 * for (int i = 0; i < 2; i++) { for (int col = 0; col <= 11;
				 * col++) { if (base.hand_view.getNumberAt(i + 10, col) == 1) {
				 * house_hands.add(new Card(col, i)); } } }
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

			Arrays.sort(allCards);
			Arrays.sort(allCardsH);
			ArrayUtils.reverse(allCards);
			ArrayUtils.reverse(allCardsH);

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
			/*
			 * for (int i = 0; i < 2; i++) { for (int col = 0; col <= 11; col++)
			 * { boolean isValue = false; if (valuesH.size() > i &&
			 * valuesH.get(i) == col) { isValue = true; }
			 * base.hand_view.changeNumberAt(i + 10, col, isValue ? 1 : 0); } }
			 */
		}

		this.ai.tick();
		boolean[] thought = this.ai.think();
		boolean shouldStay = this.getAI().getNeuronFromId(0).isTriggered();

		float accuracy = 0;

		if (!shouldLearn) {
			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A")
						.replaceAll("12", "J").replaceAll("13", "Q")
						.replaceAll("14", "K")
						+ ",");
			}
			newDeck = true;
			return (((thought.length > 2 ? this.ai.getNeuronFromId(2)
					.isTriggered() : false) ? ChatColor.GOLD
					: shouldStay ? ChatColor.BLUE : ChatColor.WHITE)
					+ ((shouldStay ? "Stay" : "Hit"))
					+ "|= "
					+ sb.toString()
					+ "|  " + shouldStay + "|Risk = " + (1.0 - Math.abs(ai
					.getNeuronFromId(0).getTriggeredStength())));

		} else {

			String reasonForOutcome = "";

			boolean noWin = false;
			boolean continuePlaying = false;
			// Action logic
			if (!shouldStay) {
				if (hands.size() <= 10) {
					// Hit
					int totalHand = 0;
					for (Card c : hands) {
						totalHand += c.value;
					}
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

						// TODO: No need to calculate if the use hit. If it's
						// under 21, its good enough
						/*
						 * wonTheGame=true; List<Card> tempHH = new
						 * ArrayList<>(house_hands); // The Dealer's AI int
						 * totalHouseHand = 0; for (Card c : tempHH) {
						 * totalHouseHand += c.value; } for (int i = 2; i < 10;
						 * i++) { if (//*game.cardsInDeck[game.currentIndex - 1
						 * + (i - 2)].value + totalHouseHand > 21|| *8/
						 * game.cardsInDeck[game.currentIndex - 1 + (i -
						 * 2)].value + totalHouseHand>=18) { break; }
						 * tempHH.add(game.cardsInDeck[game.currentIndex - 1 +
						 * (i - 2)]); totalHouseHand += tempHH.get(i).value; }
						 * if ((totalHand - cc.value) > totalHouseHand||) {
						 * wonTheGame = false; reasonForOutcome =
						 * "Hit; better if stayed"; }else{ continuePlaying =
						 * true; reasonForOutcome = "Hit; good enough"; }
						 */
					}
				} else {
					wonTheGame = false;
					reasonForOutcome = "Hit; Over 10 cards.";
				}

			} else {

				// Stay
				int totalHand = 0;
				for (Card c : hands) {
					totalHand += c.value;
				}
				if (totalHand > 21) {
					wonTheGame = false;
					reasonForOutcome = ChatColor.DARK_AQUA
							+ "How the hell did this happen!?!?";
				} else if (totalHand == 21) {
					wonTheGame = true;
					reasonForOutcome = "Stayed; got 21";
				} else {

					// The Dealer's AI
					int totalHouseHand = 0;
					for (Card ch : house_hands) {
						totalHouseHand += ch.value;
					}
					for (int i = 2; i < 10; i++) {
						if (/*
							 * game.cardsInDeck[game.currentIndex + 1].value +
							 * totalHouseHand > 21
							 */game.cardsInDeck[game.currentIndex - 1 + (i - 2)].value
								+ totalHouseHand >= 18) {
							break;
						}
						house_hands.add(game.cardsInDeck[++game.currentIndex]);
						totalHouseHand += house_hands.get(i).value;
					}

					if (totalHouseHand > 21) {
						wonTheGame = true;
						reasonForOutcome = "Stayed; dealer went over 21";
					} else if (totalHand > totalHouseHand
							&& totalHouseHand != 21) {
						// The NN is closer to 21
						// if (loops == 0) {
						wonTheGame = true;
						reasonForOutcome = "Stayed; closer to 21";
						// }
					} else {
						wonTheGame = false;
						if (house_hands.size() > 2 ? (totalHand
								+ house_hands.get(2).value <= 21)
								: (totalHand
										+ game.cardsInDeck[game.currentIndex + 1].value <= 21)) {
							reasonForOutcome = "Stayed; Should have hit";
						} else {
							noWin = true;
							reasonForOutcome = "Stayed; No-Win";
						}
					}
					// }

				}
			}
			if (continuePlaying == false)
				if ((!noWin || wonTheGame))
					this.getAccuracy().addEntry(wonTheGame);
			accuracy = (float) this.getAccuracy().getAccuracy();

			StringBuilder sb = new StringBuilder();
			for (Card c : hands) {
				sb.append((c.number + "").replaceAll("11", "A")
						.replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "q")
						+ ",");
			}
			StringBuilder sb2 = new StringBuilder();
			for (Card c : house_hands) {
				sb2.append((c.number + "").replaceAll("11", "A")
						.replaceAll("12", "j").replaceAll("13", "q")
						.replaceAll("14", "q")
						+ ",");
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
			for (Card c : hands) {
				totalHand += c.value;
			}
			for (int i = 0; i < 2; i++) {
				totalHHand += house_hands.get(i).value;
			}

			double chanceForRightCard = 0;

			for (int i = hands.size() - k; i < 52; i++) {
				if (totalHand + game.cardsInDeck[i].value <= 21
						&& totalHand + game.cardsInDeck[i].value > totalHHand)
					chanceForRightCard++;
			}

			chanceForRightCard /= (52 - hands.size() + k);
			if (chanceForRightCard < 0.5)
				lowChanceForSuccess = true;
			// Re-add the card
			if (potentialHitCard != null)
				hands.add(potentialHitCard);

			// IMPROVE IT
			HashMap<Neuron, Double> map = new HashMap<>();
			// TODO: Test if right: If staying wins or if hitting loses, set to
			// shouldStay. Else, hit
			if (shouldStay == wonTheGame) {
				map.put(ai.getNeuronFromId(0), -1.0/*-(chanceForRightCard)*/);
			} else {
				map.put(ai.getNeuronFromId(0), 1.0/* (1 - chanceForRightCard) */);
			}

			// If you won the game, or lost but was NOT in a no win
			// situation
			if ((!wonTheGame && !noWin)) {
				DeepReinformentUtil.instantaneousReinforce(this, map, 1);
			}

			if (!continuePlaying) {
				newDeck = true;
			} else {
				newDeck = false;
			}
			return (((int) (100 * accuracy))
					+ "|"
					+ (lowChanceForSuccess != shouldStay ? ChatColor.GOLD
							+ "[LOW-"
							+ ((int) (100 * (shouldStay ? 1 - chanceForRightCard
									: chanceForRightCard))) + "]"
							: "")
					+ (noWin ? ChatColor.YELLOW + ""
							: continuePlaying ? ChatColor.GRAY
									: (wonTheGame ? ChatColor.GREEN + ""
											: ChatColor.RED + "")) + "| "
					+ reasonForOutcome + "|= " + sb + "/" + sb2 + "|");

		}

	}

	@Override
	public NNBaseEntity clone() {
		BlackJackHelper thi = new BlackJackHelper(false);
		thi.ai = this.ai;
		return thi;
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator
					.sendMessage("Stop the learning before testing. use /nn stoplearning");
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
				cardValues[i] = cards.charAt(i) == 'j'
						|| cards.charAt(i) == 'k' || cards.charAt(i) == 'q' ? 10
						: cards.charAt(i) == 'a' ? 11 : Integer.parseInt(cards
								.charAt(i) + "");
			}
			for (int in = 0; in < this.hand_view.getMatrix().length; in++) {
				for (int in2 = 0; in2 < this.hand_view.getMatrix()[in].length; in2++) {
					this.hand_view.changeNumberAt(in, in2,
							cardValues.length > in && cardValues[in] == in2 ? 1
									: 0);
				}
			}

		} else {
			initiator
					.sendMessage("Provide a hand (10's should be written as j)");
		}

	}

	@Override
	public void setBase(NNBaseEntity base) {
	}

}
