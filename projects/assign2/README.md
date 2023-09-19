# CPD Assign 2 - Wordle

### Description

Our game consists in a text-based version of the Wordle Game, using the terminal as interface.
In this project, we were asked to develop a client-server system using TCP sockets in Java.
The project assignment is available [here](doc/assignment.pdf).


### Instructions

#### Preparation of the terminals:

To be able to run the game, it is necessary to open at least 3 terminals in the folder `src`. 
In the first one, you start the server with the command: `java Server 1030`. Note that 1030 is the port, usually this port is available, if it isn't try a higher port.
The number of players by default is 2 but you can specify a higher number by instead starting the server with the command: `java Server 1030 [n]` where n is the number of players in a game. 
In the others, you need to insert the command: `java Client localhost 1030`.

#### Game startup:

The next step is initialize the two (or more) clients.
For that, it is necessary that each one logs in or registers.
The players can then choose if they want the simple queue, the ranked queue or if they want to quit.
After choosing a queue and having the necessary players (in ranked queue may take longer due to rank difference but they will eventually meet), the game starts.

#### Some game info:

In the terminal where the server is running, we have access to the number of players and the word in game.
After each try from the players, we also receive their attempt in the server terminal.
In each attempt, the player receives the letters that are in the correct position and the ones that are correct.
For example, if the word in game is games and the player tries inserting times, he receives the following message: `Letters in the right position: m e s`.
Every player has a rank of 0 by default but negative ranks exist since players can get a negative score after a game ends.
