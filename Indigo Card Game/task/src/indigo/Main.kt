package indigo

fun main() {
    println("Indigo Card Game")
    var playFirstAnswer: String

    do {
        println("Play first?")
        playFirstAnswer = readLine()!!
    } while (playFirstAnswer.lowercase() !in listOf("yes", "no"))

    Game().startGame(playFirstAnswer)
}

class Game {
    private val cardDeck: CardDeck = CardDeck()
    private val human: Human = Human()
    private val computer: Computer = Computer()
    private val cardsOnTable: MutableList<Card> = mutableListOf()
    private val cardsWon: Map<Player, MutableList<Card>> = mapOf(
        Pair(human, mutableListOf()),
        Pair(computer, mutableListOf())
    )
    private var lastWinner: Player = Player()
    private var activePlayer: Player = human

    init {
        cardsOnTable.addAll(drawCardsFromDeck(4))
        println("Initial cards on the table: ".plus(cardsOnTable.joinToString(" ") { it.getType() }))

        dealCardsToPlayers()

        printCardsOnTable()
    }

    fun startGame(playerIsFirst: String) {
        activePlayer = if (playerIsFirst == "yes") human else computer

        do {
            if (human.cardsInHand.size == 0 && computer.cardsInHand.size == 0) dealCardsToPlayers()

            try {
                executePlayerTurn()
            } catch (re: RuntimeException) {
                println("Game Over")
                return
            }
        } while (cardsWon.values.stream().flatMap { it.stream() }.count() + cardsOnTable.size < 52)

        if (lastWinner in listOf(human, computer)) {
            cardsWon[lastWinner]?.addAll(cardsOnTable)
        } else  {
            if (playerIsFirst == "yes") cardsWon[human]?.addAll(cardsOnTable) else cardsWon[computer]?.addAll(cardsOnTable)
        }

        cardsOnTable.clear()

        printGameStatistics(true)
        println("Game Over")
    }

    private fun dealCardsToPlayers() {
        listOf(human, computer).forEach { player -> player.cardsInHand.addAll(drawCardsFromDeck(6)) }
    }

    private fun drawCardsFromDeck(noOfCards: Int): List<Card> {
        return cardDeck.getCards(noOfCards)
    }

    private fun executePlayerTurn() {
        val chosenCardIndex: Int

        if (activePlayer == human) {
            chosenCardIndex = human.playCard()
        } else {
            chosenCardIndex = computer.playCard(getTopCard())
            println("Computer plays ${computer.cardsInHand[chosenCardIndex].getType()}")
        }

        playCard(chosenCardIndex)
        changePlayerTurn()
    }

    private fun playCard(cardIndex: Int) {
        val topCardOnTable = getTopCard()
        val chosenCard = activePlayer.cardsInHand.removeAt(cardIndex)

        cardsOnTable.add(chosenCard)

        if (chosenCard.rank == topCardOnTable?.rank || chosenCard.suit == topCardOnTable?.suit) {
            cardsWon[activePlayer]?.addAll(cardsOnTable)
            cardsOnTable.clear()

            println("${if (activePlayer == human) "Player" else "Computer"} wins cards")
            printGameStatistics()
            lastWinner = activePlayer
        }

        printCardsOnTable()
    }

    private fun getTopCard(): Card? {
        return if (cardsOnTable.isEmpty()) null else cardsOnTable.last()
    }

    private fun printGameStatistics(isGameEnd: Boolean = false) {
        var humanScore = getScore(cardsWon[human]) ?: 0
        var computerScore = getScore(cardsWon[computer]) ?: 0
        val humanCardsWon = cardsWon[human]?.size ?: 0
        val computerCardsWon = cardsWon[computer]?.size ?: 0

        if (isGameEnd) {
            if (humanCardsWon > computerCardsWon) humanScore += 3 else computerScore += 3
        }

        println("Score: Player $humanScore - Computer $computerScore")
        println("Cards: Player $humanCardsWon - Computer $computerCardsWon")
    }

    private fun getScore(cards: List<Card>?): Int? {
        return cards?.count { it.rank in listOf("A", "10", "J", "Q", "K") }
    }

    private fun printCardsOnTable() {
        println(if (cardsOnTable.isEmpty()) "No cards on the table" else "${cardsOnTable.count()} cards on the table, and the top card is ${getTopCard()?.getType()}")
    }

    private fun changePlayerTurn() {
        activePlayer = if (activePlayer == human) computer else human
    }
}

open class Player {
    val cardsInHand: MutableList<Card> = mutableListOf()
}

class Human: Player() {
    fun playCard(): Int {
        print("Cards in hand: ")
        cardsInHand.forEachIndexed { index, card ->  print("${index + 1})${card.getType()} ")}
        println("")

        var chosenCardPosition: Int = -1

        do {
            println("Choose a card to play (1-${cardsInHand.size}):")
            try {
                val input = readLine()!!
                chosenCardPosition = if (input == "exit") throw RuntimeException("End game") else input.toInt()
            } catch (nfe: NumberFormatException) {
                // do nothing
            }
        } while (chosenCardPosition !in 1..6)

        return chosenCardPosition - 1
    }
}

class Computer: Player() {
    fun playCard(topCard: Card?): Int {
        println(cardsInHand.joinToString(" ") { it.getType() })

        return if (topCard == null) {
            chooseCardToPlay()
        } else {
            val candidateCards: List<Card> = getCandidateCards(topCard)

            when(candidateCards.size) {
                0 -> chooseCardToPlay()
                1 -> cardsInHand.indexOf(candidateCards.first())
                else -> chooseCandidateCardToPlay(topCard)
            }
        }
    }

    private fun chooseCardToPlay(): Int {
        val cardsOfSameSuit = cardsInHand.groupBy { it.suit }.filterValues { it.size > 1 }
        var cardToPlay = cardsInHand.random()

        if (cardsOfSameSuit.isEmpty()) {
            val cardsOfSameRank = cardsInHand.groupBy { it.rank }.filterValues { it.size > 1 }

            if (cardsOfSameRank.isNotEmpty()) {
                // neither suit nor rank
                cardToPlay = cardsOfSameRank.values.random().first()
            }
        } else {
            // same rank
            cardToPlay = cardsOfSameSuit.values.random().first()
        }

        // same suit
        return cardsInHand.indexOf(cardToPlay)
    }

    private fun chooseCandidateCardToPlay(topCard: Card): Int {
        val candidateCards = getCandidateCards(topCard)
        val topCardSuit: String = topCard.suit
        val cardsOfSameSuit = candidateCards.filter { it.suit == topCardSuit }

        return if (cardsOfSameSuit.isEmpty()) {
            val topCardRank: String = topCard.rank
            val cardsOfSameRank = candidateCards.filter { it.rank == topCardRank }

            if (cardsOfSameRank.size > 1) {
                cardsInHand.indexOf(cardsOfSameRank.random())
            } else if (cardsOfSameRank.size == 1) {
                cardsInHand.indexOf(cardsOfSameRank.first())
            } else {
                cardsInHand.indexOf(candidateCards.random())
            }
        } else {
            if (cardsOfSameSuit.size == 1) {
                cardsInHand.indexOf(cardsOfSameSuit.first())
            } else {
                cardsInHand.indexOf(cardsOfSameSuit.random())
            }
        }
    }

    private fun getCandidateCards(topCard: Card): List<Card> = cardsInHand.filter { it.rank == topCard.rank || it.suit == topCard.suit }
}

class CardDeck {
    private var cards: MutableList<Card> = mutableListOf()

    init {
        cards = assembleDeck()
    }

    fun getCards(noOfCards: Int): List<Card> {
        if (noOfCards > cards.size) {
            throw IllegalArgumentException("The remaining cards are insufficient to meet the request.")
        }

        val cardsToRemove: MutableList<Card> = mutableListOf()

        for (i in 0 until noOfCards) {
            cardsToRemove.add(cards[i])
        }

        cards.removeAll(cardsToRemove)

        return cardsToRemove
    }
    
    private fun assembleDeck(): MutableList<Card> {
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val suits = listOf("♦", "♥", "♠", "♣")
        val deck = mutableListOf<Card>()
        
        ranks.forEach { rank -> suits.forEach { deck.add(Card(rank, it)) } }

        return deck
    }
}

data class Card(val rank: String, val suit: String) {
    fun getType(): String = rank + suit
}