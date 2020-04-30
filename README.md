# LegoScorer
For managing the Ontario Tech Engineering Robotics Competition

## Introduction
The LegoScorer program is essentially a tournament managing program, used to handle the yearly Ontario Tech Engineering Robotics Competition at Ontario Tech University. It is designed to handle any game design and supports qualification and elimination matches, as well as schedule generation. 

## Installation
The releases tab contains the most recent version of the LegoScorer application. It can be run on Windows or MacOS (download your version). If neither of these suits your needs, you can download the source code and build it using JDK14. To repackage this program, I used jlink and jpackage (jdk14+), but for a single computer, you can simply just run the program using your local jdk. 

## Basics
The program has three menus. When you first open it, you can create a tournament, create a game type, or import a tournament. Tournaments and game types are both saved as data files (.tdat and .gdat), and can be edited manually if required. The first step is to create a game type, modelling the current year's game. The second step is to create a tournament, using the game type as well as a team list. After both of these criteria are met, you can open the tournament itself and edit match data. 

## Important notes
All data (.gdat, .tdat) files will be stored in a user's Documents/LegoScorer folder by default. This location can be changed within the LegoScorer program itself, and these files can be transferred between PCs without risk. Once a tournament is created, the game data file is not required, as all the data gets imported into the tournament data file.

Once a tournament is created, it creates a linked excel file containing all important data. It will be in the same folder as the tournament file itself, and can be used to update a scoreboard if needed. 

Once a playoff round is generated, it *cannot* be removed unless manually removed by editing the .tdat file. An update will resolve this partially. However, generating the next playoffs round (ex. generating semifinals when quarterfinals are not finished) has inherent problems, as re-generating the semifinals after changing quarterfinals scores would delete semifinals scores, and result in match replays. Do not generate a set of matches unless you are sure that the previous scores entered are correct, and that the next set of matches is about to be played. 
