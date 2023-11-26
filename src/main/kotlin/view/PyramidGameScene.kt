package view

import entity.Card
import entity.Player
import service.CardImageLoader
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.util.Stack
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

class PyramidGameScene (private val rootService: RootService) : BoardGameScene(1920, 1240), Refreshable {

    //Player Names
    private val labelWidth = 550
    private val labelHeight = 50
    private val sideMargin = 60 // Adjust this margin to move labels closer to or further from the sides

    //Player Points
    private val pointsLabelWidth = 150
    private val pointsLabelHeight = 30


    private val player1NameLabel = Label(
        posX = sideMargin, // X position from the left edge
        posY = 50,
        width = labelWidth,
        height = labelHeight,
        text = "Player 1"
    ).apply {
        font = Font(size = 67, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
    }

    private val player2NameLabel = Label(
        posX = 1920 - labelWidth - sideMargin, // X position from the right edge
        posY = 50,
        width = labelWidth,
        height = labelHeight,
        text = "Player 2"
    ).apply {
        font = Font(size = 67, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
    }


    private val player1PointsLabel = Label(
        posX = player1NameLabel.posX + (labelWidth - pointsLabelWidth) / 2, // Center under the player name label
        posY = player1NameLabel.posY + labelHeight + 85, // Below the player name label
        width = pointsLabelWidth,
        height = pointsLabelHeight,
        text = "0"
    ).apply {
        font = Font(size = 80, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
    }

    private val player2PointsLabel = Label(
        posX = player2NameLabel.posX + (labelWidth - pointsLabelWidth) / 2, // Center under the player name label
        posY = player2NameLabel.posY + labelHeight + 85, // Below the player name label
        width = pointsLabelWidth,
        height = pointsLabelHeight,
        text = "0"
    ).apply {
        font = Font(size = 80, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
    }



    private val drawPile = LabeledStackView(posX = 270, posY = 500, "Draw Pile").apply {
        onMouseClicked = {

            if(selectedCards.isNotEmpty()){
                selectedCards[0].posY += 30
            }

            selectedCards.clear()

            if(reservePile.isNotEmpty()){
                moveCardView(reservePile.peek(), reservePile)
            }

            rootService.currentGame?.let { game ->
                // Determine which player's turn it is and perform the action
                if (game.playerOnesTurn) {
                    rootService.playerActionService.useDrawPile(game.player1)
                } else {
                    rootService.playerActionService.useDrawPile(game.player2)
                }
            }

        }
    }

    private val reservePile = LabeledStackView(posX = 1520, posY = 500, "Reserve Pile")

    // Pass Button
    private val passButton = Button(
        width = 260,
        height = 120,
        posX = 1565,       // Centered horizontally
        posY = 1040,       // Positioned at the bottom
    ).apply {
        visual = ImageVisual("PassButton.png")
        onMouseClicked = {
            refreshAfterPass(currentPlayer())
        }
    }

    private val pyramidLayout: MutableList<MutableList<CardView>> = mutableListOf()

    private val cardMap: BidirectionalMap<Card, CardView> = BidirectionalMap()

    private var selectedCards: MutableList<CardView> = mutableListOf()





    init {
        background = ColorVisual(57, 70, 59)

        addComponents(
            drawPile,
            reservePile,
            player1NameLabel,
            player2NameLabel,
            player1PointsLabel,
            player2PointsLabel,
            passButton
        )
    }





    private fun currentPlayer(): Player{
        val game = rootService.currentGame
        checkNotNull(game)
        return if(game.playerOnesTurn) game.player1 else game.player2
    }
    private fun updatePlayerPoints() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        // Update the labels with the current points from the game state
        player1PointsLabel.text = "${game.player1.score}"
        player2PointsLabel.text = "${game.player2.score}"
    }
    private fun highlightCurrentPlayer() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        // Reset styles for both player labels to default
        player1NameLabel.font = Font(size = 67, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
        player2NameLabel.font = Font(size = 67, color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )

        // Highlight the label of the current player
        if (game.playerOnesTurn) {
            player1NameLabel.font = Font(size = 67, color = Color(208, 0, 0), fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
        } else {
            player2NameLabel.font = Font(size = 67, color = Color(208, 0, 0), fontWeight = Font.FontWeight.BOLD, family = "Copperplate" )
        }
    }




    private fun initializePyramid(pyramidCards: List<Card>) {
        val cardWidth = 100
        val cardHeight = 150
        val cardSpacing = 167 // Spacing between cards
        val hOverlap = (cardWidth / 2) - (cardSpacing / 2)
        val vOverlap = (cardHeight / 2) - (cardSpacing / 2)
        val sceneWidth = 1920
        pyramidLayout.clear()
        var currentY = 68
        var cardIndex = 0

        for (row in 1..7) {
            val rowCards = mutableListOf<CardView>()
            val offsetX = (cardWidth - hOverlap)
            val rowWidth = (row * cardWidth) - (row - 1) * (offsetX - cardSpacing)
            var currentX = (sceneWidth - rowWidth) / 2

            for (col in 1..row) {
                val card = pyramidCards[cardIndex++]
                val isEdgeCard = col == 1 || col == row
                val cardView = initializeCardView(card, isEdgeCard)

                cardView.posX = currentX.toDouble()
                cardView.posY = currentY.toDouble()
                addComponents(cardView)
                rowCards.add(cardView)
                currentX += offsetX

                cardMap.add(card to cardView)
            }

            pyramidLayout.add(rowCards)
            currentY += (cardHeight - vOverlap)

        }
        println("Pyramid has been initialised. Current Cards: ${rootService.currentGame?.pyramid?.flatten()?.size}")
        println(rootService.currentGame?.pyramid?.flatten())
    }
    private fun clearPyramidLayout() {
        for (row in pyramidLayout) {
            for (cardView in row) {
                removeComponents(cardView)
            }
        }
        pyramidLayout.clear()
    }



    private fun isEdgeCard(cardView: CardView): Boolean {
        for (row in pyramidLayout) {
            if (row.contains(cardView)) {
                // Check if the card is the first or the last in its row
                return row.indexOf(cardView) == 0 || row.indexOf(cardView) == row.size - 1
            }
        }
        return false
    }
    private fun flipNewEdgeCards() {
        for (row in pyramidLayout) {
            for (cardView in row) {
                if (isEdgeCard(cardView)) {
                    cardView.showFront()
                    cardMap.backward(cardView).flipped = true
                    cardView.onMouseClicked = {
                        handleCardSelection(cardView)
                    }
                }
            }
        }
    }

    private fun isCardOnReservePile(card: CardView): Boolean {
        val game = rootService.currentGame
        checkNotNull(game)

        // Check if the reserve pile is not empty and the top card matches the given card
        return game.storagePile.isNotEmpty() && cardMap.forward(game.storagePile.peek()) == card
    }

    private fun handleCardSelection(cardView: CardView) {

        val game = rootService.currentGame
        checkNotNull(game)

        // Check if the card is already selected
        if (selectedCards.contains(cardView)) {

            // Deselect the card
            if(reservePile.isNotEmpty() && reservePile.peek() == cardView){
                moveCardView(reservePile.peek(), reservePile)
            } else {
                cardView.posY += 30
            }

            selectedCards.remove(cardView)

        } else if(selectedCards.size == 0) {

            cardView.posY -= 30 // Adjust this value to change the elevation effect
            selectedCards.add(cardView)

        } else if (selectedCards.size == 1) {
            cardView.posY -= 30 // Adjust this value to change the elevation effect
            selectedCards.add(cardView)

            val card1 = cardMap.backward(selectedCards[0])
            val card2 = cardMap.backward(selectedCards[1])

            // Delay time for the card animation
            this@PyramidGameScene.lock()
            this@PyramidGameScene.playAnimation(DelayAnimation(duration = 500).apply{
                onFinished = {

                    //--------------------------------------------
                    if (rootService.gameService.checkPair(card1, card2) &&
                       (isEdgeCard(selectedCards[0]) && isEdgeCard(selectedCards[1]) ||
                       (isEdgeCard(selectedCards[0]) && isCardOnReservePile(selectedCards[1])) ||
                       (isEdgeCard(selectedCards[1]) && isCardOnReservePile(selectedCards[0])))) {

                        println("Removed Cards: ${card1.toString()} , ${card2.toString()}")

                        rootService.playerActionService.selectPair(currentPlayer(), card1, card2)
                        highlightCurrentPlayer()

                    } else {

                        if(reservePile.isNotEmpty()){
                            selectedCards.forEach { card ->
                                if(card == reservePile.peek()) moveCardView(reservePile.peek(), reservePile)
                                else card.posY += 30
                            }
                        } else {
                            selectedCards.forEach { card -> card.posY += 30 }
                        }
                        println("Invalid Move")
                        selectedCards.clear()
                    }
                    //--------------------------------------------

                    this@PyramidGameScene.unlock()
                } })
        }
    }


    private fun initializeStackView(stack: Stack<Card>, stackView: LabeledStackView, cardImageLoader: CardImageLoader){
        stackView.clear()
        stack.peekAll().forEach(){ card ->
            val cardView = CardView(
                height = 165,
                width = 110,
                front = ImageVisual(cardImageLoader.frontImageFor(card.suit, card.value)),
                back = ImageVisual(cardImageLoader.backImage)
            )
            stackView.add(cardView)
            cardMap.add(card to cardView)
        }
    }
    private fun initializeCardView(card: Card, isEdgeCard: Boolean): CardView {
        val cardImageLoader = CardImageLoader()

        val cardView = CardView(
            height = 150,
            width = 100,
            front = ImageVisual(cardImageLoader.frontImageFor(card.suit, card.value)),
            back = ImageVisual(cardImageLoader.backImage)
        )

        // Show the front for edge cards, back for non-edge cards
        if (isEdgeCard) {
            cardView.showFront()
        } else {
            cardView.showBack()
        }

        cardView.onMouseClicked = {
            handleCardSelection(cardView)
        }

        return cardView
    }



    override fun refreshAfterStartNewGame() {

        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }


        player1NameLabel.text = game.player1.name
        player2NameLabel.text = game.player2.name
        highlightCurrentPlayer()

        // Initialize points labels with zero points
        player1PointsLabel.text = "0"
        player2PointsLabel.text = "0"

        clearPyramidLayout()
        selectedCards.clear()
        cardMap.clear()
        val cardImageLoader = CardImageLoader()


        initializeStackView(game.drawPile, drawPile, cardImageLoader)
        initializeStackView(game.storagePile, reservePile, cardImageLoader)


        val pyramidCards = game.pyramid.flatten()
        initializePyramid(pyramidCards)
    }
    override fun refreshAfterRemovePair(player: Player, removedCards: List<Card>){
        val game = rootService.currentGame
        checkNotNull(game)

        if(rootService.gameService.checkPair( removedCards[0], removedCards[1])){

            selectedCards.forEach(){ card ->
                if(reservePile.isNotEmpty() && card == reservePile.peek()){
                    reservePile.pop()
                } else removeComponents(card)
            }



            for(row in pyramidLayout){
                row.remove(selectedCards.first())
                row.remove(selectedCards.last())
            }



            selectedCards.clear()

            flipNewEdgeCards()
        }
        else {
            selectedCards.clear()
        }

        updatePlayerPoints()
        highlightCurrentPlayer()



        if(game.pyramid.flatten().isEmpty()) rootService.gameService.endGame()

    }
    override fun refreshAfterPass(player: Player) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        // Implement the logic for passing the turn
        // For example, toggle the player's turn and update the game state
        game.passCounter++

        if(game.passCounter == 2){
            rootService.gameService.endGame()
        }

        game.playerOnesTurn = !game.playerOnesTurn
        highlightCurrentPlayer()

        // You might also want to update other parts of the UI or game state as needed
    }
    override fun refreshAfterRevealCard(player: Player, revealedCard: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        highlightCurrentPlayer()

        when (player) {
            game.player1 -> moveCardView(cardMap.forward(game.storagePile.peek()),reservePile, true)
            game.player2 -> moveCardView(cardMap.forward(game.storagePile.peek()),reservePile, true)
        }



        if(reservePile.isNotEmpty()){
            reservePile.peek().apply {
                onMouseClicked = {
                    handleCardSelection(reservePile.peek())
                }
            }
        }

        if(game.drawPile.isEmpty()){
            drawPile.clear()
            drawPile.onMouseClicked = null
        }
    }


    /**
     * moves a [cardView] from current container on top of [toStack].
     *
     * @param flip if true, the view will be flipped from [CardView.CardSide.FRONT] to
     * [CardView.CardSide.BACK] and vice versa.
     */
    private fun moveCardView(cardView: CardView, toStack: LabeledStackView, flip: Boolean = false) {
        if (flip) {
            when (cardView.currentSide) {
                CardView.CardSide.BACK -> cardView.showFront()
                CardView.CardSide.FRONT -> cardView.showBack()
            }
        }
        cardView.removeFromParent()
        toStack.add(cardView)
    }


}
