# love-letter-AI
An Artificial Intelligence for the card game Love Letter, using counterfactual regret minimization.

### Abstract

I set out to create an Artificial Intelligence that could play the two-player version of the card game Love Letter reasonably well against an amateur human opponent. I succesfully created a program that could play perfectly or near-perfectly on significantly reduced versions of the game, using counterfactual regret minimization to find exact and approximate Nash equilibria. I expect that with future optimizations and/or significant additional compute time, the complete game could be solved with this technique.

### The Game

Love Letter is a game that uses a custom deck of 16 cards. On each turn, a player starts with a card in their hand and draws a card from the deck, and then chooses to play one of those cards. Each card has a special ability that is activated when played. The game ends when, because of the cards' abilities, one player is out, or when there is nothing left to draw from the deck. In the two-player version, three cards are removed from the deck and placed face up at the beginning, and one card is removed and placed face down. Each player starts with one card, and there are 11 cards remaining in the deck at the start of the game. In this paper, I also consider reduced versions of the game where more than three cards are set face up at the beginning, and so the remaining deck is smaller. This can significantly reduce the amount of computation needed.

### Approaches

 - Monte Carlo Tree Search, both UCTS-1 or w/e and that other thingss
 - Counterfactual Regret Minimization
 
 ### Challenges
 
 ### Results
 
 ### Future Work

  - Sampling
  - Merging information sets
    - Imperfect Recall
