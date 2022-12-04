# Patchwork
PC version of the Patchwork game in Java

# Todo

- complete/enhance javadoc (and beautify with options in build.xml)
- Last thing to do before merge is to fix the advance Action
(As it's a possibility that all 3 patches the user can buy have prices > number of buttons of player
The player has to be able to advance every Round if he is not out of bond of the board (< 0 && > spaces)
Because the player that is doing the action is always the latest player so there's always a player in front of him
And remember if 2 players share the same position the player that got here last (so the player that just played an action)
is the one to play again)
- Play the Game and fix bugs if encounters one then merge into main

#  Files

```sh
├── Patchwork/ 			<-- The code
└── ressources/			<-- Useful ressources for the project
    ├── cards				<-- images of the card 
    ├── lib				<-- Zen5
    ├── rules				<-- img of rules / rules summary in french
    └── uml				<-- some conception draft (uml mostly)
```

