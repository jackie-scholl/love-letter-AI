# love-letter-AI
An Artificial Intelligence for the card game Love Letter, using counterfactual regret minimization.

If you're exploring this project for the first time, you might start with:
- [FullGameState.java](src/com/github/raptortechjs/LoveLetter/TwoPlayer/Game/FullGameState.java) has most of the logic for implementing the core game
- [CounterfactualRegret.java](src/com/github/raptortechjs/LoveLetter/TwoPlayer/AI/CounterfactualRegret.java) implements the Counterfactual Regret Minimization algorithm
- [GameStateDistribution.java](src/com/github/raptortechjs/LoveLetter/TwoPlayer/AI/GameStateDistribution.java) calculates the probabilities of various game states occuring

### Abstract

I set out to create an Artificial Intelligence that could play the two-player version of the card game Love Letter reasonably well against an amateur human opponent. I succesfully created a program that could play near-perfectly on significantly reduced versions of the game, using counterfactual regret minimization to find approximate Nash equilibria. I expect that with future optimizations and significant additional compute time, the complete game could be solved with this technique.

### The Game

Love Letter is a game that uses a custom deck of 16 cards. On each turn, a player starts with a card in their hand and draws a card from the deck, and then chooses to play one of those cards. Each card has a special ability that is activated when played. The game ends when, because of the cards' abilities, one player is out, or when there is nothing left to draw from the deck. In the two-player version, three cards are removed from the deck and placed face up at the beginning, and one card is removed and placed face down. Each player starts with one card, and there are 11 cards remaining in the deck at the start of the game. In this paper, I also consider reduced versions of the game where more than three cards are set face up at the beginning, and so the remaining deck is smaller. This can significantly reduce the amount of computation needed.

### Approaches
- Monte Carlo Tree Search, both UCTS-1 or w/e and that other things (Information Set)
- Counterfactual Regret Minimization
 
 One common approach to games of this type is Monte Carlo Tree Search, or MCTS. The basic idea behind MCTS is that the algorithm simulates playing randomly from the root of tree a bunch of times, and as it plays it builds up information about the likelihood of winning from any given state, which is then takes advantage of in its random playouts to start playing better. The end result is that nodes have attached information about how the computer should play from that state, which the computer then uses to select an action in the actual game. The basic MCTS algorithm assumes perfect information, but it can handle randomness.
 
 There are two major approaches to handling games of imperfect information with MCTS: determinization, and information sets. In determinization, the algorithm samples or computes the probability of different possible histories given the information about the game state currently available to the player (called the player's information set). From each of possible histories, the algorithm applies a form of minimax or expectimax to determine the best move and expected winnings, and then the probabilities are combined with the expected winnings and best moves to come up with a best move for the player given what they can see. One limitation of this approach is that it is "averaging over clairvoyance"; the best moves in each state are selected assuming that both players can see the entire state for the rest of the game, which limits the ability of the algorithm to account for the effects of information being hidden. Another approach is Information Set Monte Carlo Tree Search. In this approach, the search is not done over a tree of actual game states, but instead over a tree of information sets. A limitation of this approach is that it does not consider belief distributions over the hidden information (e.g., an opponent's hand).
 
 Another possible algorithm for playing games of this type is regret minimization. The idea behind regret minimization is that we can compare an action we took in a situation and the final result and another action we could have taken in that situation and what the final result would have been if we had taken that action, and minimize the regret we feel for not taking those other actions. It has been shown that over many iterations we approach a Nash equilibrium. Counterfactual regret minimization is a way of building that approximate Nash equilibrium faster by exploiting the structure of the game to break down overall regret into subterms that can be minimized independently.
 
 I chose counterfactual regret minimization for this problem because it implicitly handles belief distributions over hidden information, it uses upfront work to build a lookup table that allows for extremely fast play, and it can return a bound on the exploitability of the resulting algorithm so you can be confident that the resulting algorithm is strong against all play.
 
 ### Challenges
 
 It took a significant amount of work to get the basic algorithm working, so I didn't have a lot of time for optimizations. Counterfactual regret minimization is a very expensive algorithm when significant optimizations are not used, so it took a lot of computational power to calculate policies for even the simplest of games.
 
 ### Results
 
 I was able to calculate a complete policy with an exploitability of less than 5% for the 4-card version of the game. The 4-card version is the simplest possible reduction that still involves choices; it starts with each player having one card in their hand, one card being set aside, one card in the deck, and 12 cards placed face-up. This computation took two hours to complete on Dr. Doucette's research server. I was also able to calculate policies for larger versions of the game with up to 7 cards assuming a fixed set of cards that are set aside face-up at the beginning. For several of these versions I was able to find precise Nash equilibria. I believe that the ability to do this requires certain cards, such as the Princes, to be set aside so that the Nash equilibria can be pure, and therefore precisely calculable.
 
 ### Future Work

 I believe this work can be significantly expanded in the future by using sampling to avoid the expensive analytical work of computing a probability distribution over the whole game, and also by treating multiple information sets as one so that we can get reasonably good policies with fewer samples. One possible approach to treating multiple information sets as one is imperfect recall, where we only consider the last k moves for the purpose of equality, effectively "forgetting" older moves. This ensures that the information sets that we consider as the same are, in fact, reasonably similar, and therefore we can expect that a single policy should work reasonably well for all of them.
 
 Another option is to abandon the idea of counterfactual regret minimization and switch to some form of Monte Carlo Tree Search. I suspect that determinization might not actually be that bad in this case.
 
 ### References
 
  - I relied heavily on [*Regret Minimization in Games with Incomplete Information*](http://poker.cs.ualberta.ca/publications/NIPS07-cfr.pdf) by Zinkevich et. al., which introduced the idea of Counterfactual Regret Minimization, to implement the algorithm.
  - I also read [*Information Set Monte Carlo Tree Search*](http://ieeexplore.ieee.org/document/6203567/) by Cowling et. al., which introduced the idea of Information Set Monte Carlo Tree Search and helpfully explained the different approaches for applying MCTS to games of imperfect information and the advantages and drawbacks of each, which was very helpful when deciding which approach to take.
  - I could not possibly have done this without the help of Dr. John Doucette.
