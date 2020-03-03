package board_games.chess

@DslMarker
annotation class BoardMaker

@DslMarker
annotation class BoardListenerMarker

@DslMarker
annotation class PieceMarker

interface BoardListener {
    fun pawnPromotion(color: Color): Piece?
    fun boardUpdate(board: Array<Array<PublicCell>>)
    fun check(pieceCell: PublicCell, kingPiece: PublicCell)
    fun captured(piece: Piece)
}

@BoardMaker
class BoardBuilder {
    private var promotePawn: (Color) -> Piece? = { null }

    @BoardListenerMarker
    fun pawnPromotion(block: (Color) -> Piece?) {
        promotePawn = block
    }

    private var updateBoard: (Array<Array<PublicCell>>) -> Unit = {}

    @BoardListenerMarker
    fun boardUpdate(block: (Array<Array<PublicCell>>) -> Unit) {
        updateBoard = block
    }

    private var checkPiece: (PublicCell, PublicCell) -> Unit = { _, _ -> }

    @BoardListenerMarker
    fun checkListen(block: (PublicCell, PublicCell) -> Unit) {
        checkPiece = block
    }

    private var captureListener: (Piece) -> Unit = {}

    @BoardListenerMarker
    fun capture(block: (Piece) -> Unit) {
        captureListener = block
    }


    @BoardMaker
    var moveCount: Int = 0

    private var fenInfo: String? = null

    private var customBoard: Array<Array<Cell>> = Array(8) { x -> Array(8) { y -> Cell(x, y) } }

    @PieceMarker
    fun pawn(x: Int, y: Int, color: Color, block: Pawn.() -> Unit = {}) {
        customBoard[x][y].piece = Pawn(color).apply(block)
    }

    @PieceMarker
    fun rook(x: Int, y: Int, color: Color, block: Rook.() -> Unit = {}) {
        customBoard[x][y].piece = Rook(color).apply(block)
    }

    @PieceMarker
    fun bishop(x: Int, y: Int, color: Color, block: Bishop.() -> Unit = {}) {
        customBoard[x][y].piece = Bishop(color).apply(block)
    }

    @PieceMarker
    fun knight(x: Int, y: Int, color: Color, block: Knight.() -> Unit = {}) {
        customBoard[x][y].piece = Knight(color).apply(block)
    }

    @PieceMarker
    fun queen(x: Int, y: Int, color: Color, block: Queen.() -> Unit = {}) {
        customBoard[x][y].piece = Queen(color).apply(block)
    }

    @PieceMarker
    fun king(x: Int, y: Int, color: Color, block: King.() -> Unit = {}) {
        customBoard[x][y].piece = King(color).apply(block)
    }

    @BoardMaker
    fun fromFEN(fen: String) {
        fenInfo = fen
    }

    @BoardMaker
    private fun fromFENInfo(fen: String): Pair<Array<Array<Cell>>, Int> {
        val numList = arrayListOf("8", "88", "888", "8888", "88888", "888888", "8888888", "88888888")
        val board = Array(8) { x -> Array(8) { y -> Cell(x, y) } }
        val s = fen.split("/".toRegex()).toTypedArray()
        val other = s[7].substring(s[7].indexOf(" ") + 1).split(" ".toRegex())
        val moveCount = Integer.parseInt(other[other.size - 1])
        s[7] = s[7].substring(0, s[7].indexOf(" "))
        for (i in s.indices) {
            for (j in s[i].indices) {
                if (!Character.isLetter(s[i][j]) && Character.getNumericValue(s[i][j]) != 8) {
                    val num = s[i].substring(j, j + 1).toInt()
                    s[i] = s[i].replace("" + num, numList[num - 1])
                }
            }
        }
        for (i in 0..7) {
            for (j in s[i].indices) {
                if (s[i] == "8") break
                board[i][j].piece = when (s[i][j]) {
                    'R' -> Rook(Color.WHITE)
                    'r' -> Rook(Color.BLACK)
                    'K' -> King(Color.WHITE)
                    'k' -> King(Color.BLACK)
                    'B' -> Bishop(Color.WHITE)
                    'b' -> Bishop(Color.BLACK)
                    'P' -> Pawn(Color.WHITE)
                    'p' -> Pawn(Color.BLACK)
                    'Q' -> Queen(Color.WHITE)
                    'q' -> Queen(Color.BLACK)
                    'N' -> Knight(Color.WHITE)
                    'n' -> Knight(Color.BLACK)
                    else -> null
                }
            }
        }
        customBoard = board
        return board to moveCount//other[1] == "w"
    }

    @BoardMaker
    fun build() = Board(object : BoardListener {
        override fun boardUpdate(board: Array<Array<PublicCell>>) = updateBoard(board)
        override fun pawnPromotion(color: Color): Piece? = promotePawn(color)
        override fun check(pieceCell: PublicCell, kingPiece: PublicCell) = checkPiece(pieceCell, kingPiece)
        override fun captured(piece: Piece) = captureListener(piece)
    }).apply {
        this@BoardBuilder.fenInfo?.let {
            val pair = this@BoardBuilder.fromFENInfo(it)
            this.board = pair.first
            this.moveCount = pair.second
        } ?: run {
            this.board = this@BoardBuilder.customBoard
            this.moveCount = moveCount
        }

    }
}

private class DefaultListener : BoardListener {
    override fun pawnPromotion(color: Color): Piece? = null
    override fun boardUpdate(board: Array<Array<PublicCell>>) = Unit
    override fun check(pieceCell: PublicCell, kingPiece: PublicCell) = Unit
    override fun captured(piece: Piece) = Unit
}

@BoardMaker
class Board(private val listener: BoardListener = DefaultListener()) {

    internal var board: Array<Array<Cell>> = Array(8) { x -> Array(8) { y -> Cell(x, y) } }

    val publicBoard get() = board.toPublicCell()

    var moveCount: Int = 0
        internal set

    companion object {
        @BoardMaker
        operator fun invoke(block: BoardBuilder.() -> Unit) = BoardBuilder().apply(block).build()
    }

    private val capturedList: MutableList<Piece> = mutableListOf()
    fun getCaptured(color: Color): List<Piece> = capturedList.groupBy(Piece::color).getOrDefault(color, emptyList())

    internal operator fun get(index: Int) = board[index]
    operator fun iterator() = board.iterator()

    private fun promotePawn(cell: Cell) {
        cell.piece = listener.pawnPromotion(cell.piece!!.color) ?: cell.piece
    }

    private fun isPromotional(toCell: Cell): Boolean = toCell.piece is Pawn && (toCell.x == 0 || toCell.x == 7)

    fun customToString(block: (Array<Array<PublicCell>>) -> String) = board.toPublicCell().let(block)
    override fun toString(): String =
        board.joinToString("\n") { it.joinToString { cell -> "${cell.character}${cell.y} - ${cell.piece?.unicodeSymbol ?: "  "}" } }

    private fun Array<Array<Cell>>.toPublicCell() =
        map { columns -> columns.map { it.toPublicCell() }.toTypedArray() }.toTypedArray()

    fun move(movement: Pair<String, String>) = move(
        movement.first[0].toUpperCase() - 'A' to (movement.first[1] - 48).toInt(),
        movement.second[0].toUpperCase() - 'A' to (movement.second[1] - 48).toInt()
    )

    fun move(movement: Pair<Pair<Pair<Int, Int>, Int>, Int>, boolean: Boolean = true) =
        move(movement.first.first.first, movement.first.first.second, movement.first.second, movement.second)

    fun move(from: Pair<Int, Int>, to: Pair<Int, Int>) = move(from.first, from.second, to.first, to.second)
    fun move(fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val fromCell = board[fromX][fromY]
        val toCell = board[toX][toY]
        return if (fromCell.piece?.canMove(this, fromCell, toCell) == true) {
            toCell.piece?.let {
                listener.captured(it)
                capturedList += it
            }
            toCell.piece = fromCell.piece
            fromCell.piece = null
            listener.boardUpdate(board.toPublicCell())
            if (isPromotional(toCell)) {
                promotePawn(toCell)
                listener.boardUpdate(board.toPublicCell())
            }
            checkForCheck()
            true
        } else {
            false
        }.also { if (it) moveCount++ }
    }

    private fun checkForCheck() = Color.values().forEach {
        var kingCell: Cell? = null
        findLoop@ for (x in 0..7) {
            for (y in 0..7) {
                if (board[x][y].piece is King && board[x][y].piece?.color == it) {
                    kingCell = board[x][y]
                    break@findLoop
                }
            }
        }
        checkingLoop@ for (x in 0..7) {
            for (y in 0..7) {
                val cell = board[x][y]
                if (kingCell?.let { it1 -> cell.piece?.canMove(this, cell, it1) } == true &&
                    kingCell.piece != cell.piece) {
                    listener.check(cell.toPublicCell(), kingCell.toPublicCell())
                    break@checkingLoop
                }
            }
        }
    }

    fun setup() {
        pawnSetup()
        rookSetup()
        knightSetup()
        bishopSetup()
        queenSetup()
        kingSetup()
    }

    private fun pawnSetup() {
        for (i in 0 until 8) {
            board[1][i].piece = Pawn(Color.WHITE).apply {
                enPassant = {
                    listener.captured(it.piece!!)
                    capturedList += it.piece!!
                    it.piece = null
                }
            }
            board[6][i].piece = Pawn(Color.BLACK).apply {
                enPassant = {
                    listener.captured(it.piece!!)
                    capturedList += it.piece!!
                    it.piece = null
                }
            }
        }
    }

    private fun rookSetup() {
        board[0][0].piece = Rook(Color.WHITE)
        board[0][7].piece = Rook(Color.WHITE)
        board[7][0].piece = Rook(Color.BLACK)
        board[7][7].piece = Rook(Color.BLACK)
    }

    private fun knightSetup() {
        board[0][1].piece = Knight(Color.WHITE)
        board[0][6].piece = Knight(Color.WHITE)
        board[7][1].piece = Knight(Color.BLACK)
        board[7][6].piece = Knight(Color.BLACK)
    }

    private fun bishopSetup() {
        board[0][2].piece = Bishop(Color.WHITE)
        board[0][5].piece = Bishop(Color.WHITE)
        board[7][2].piece = Bishop(Color.BLACK)
        board[7][5].piece = Bishop(Color.BLACK)
    }

    private fun queenSetup() {
        board[0][3].piece = Queen(Color.WHITE)
        board[7][3].piece = Queen(Color.BLACK)
    }

    private fun kingSetup() {
        board[0][4].piece = King(Color.WHITE)
        board[7][4].piece = King(Color.BLACK)
    }

}

data class Cell(val x: Int, val y: Int, var piece: Piece? = null) {
    val character = ('A' + x).toString()
    fun isEmpty() = piece == null
    fun toPublicCell() = PublicCell(x, y, character, piece)
    infix operator fun contains(piece: Piece) = this.piece == piece
}

data class PublicCell(val x: Int, val y: Int, val character: String, val piece: Piece?)

fun Board.toFEN(lastMove: String? = null): String {
    var s = ""
    for (i in 0..7) {
        for (j in 0..7) {
            val p: Piece? = board[i][j].piece
            s += if (!board[i][j].isEmpty() && p != null) p.symbol
                .let { if (p.color == Color.WHITE) it.toUpperCase() else it.toLowerCase() } else "8"
        }
        s += "/"
    }
    s = s.replace("//", "/")
    s = s.substring(0, s.length - 1)
    val s1 = s.split("/".toRegex()).toTypedArray()
    for (q in s1.indices) {
        val s2 = s1[q]
        var s3: String
        if (s2.contains("8")) {
            var count = 0
            s3 = ""
            for (i in 0..s2.length) {
                if (i == s2.length) {
                    if (count > 0) {
                        var re = ""
                        for (j in 0 until count) {
                            re += "8"
                        }
                        if (re != "") s3 = s3.replace(re, "" + count)
                    }
                    count = 0
                } else if (s2[i] != '8' && count > 0) {
                    var re = ""
                    for (j in 0 until count) {
                        re += "8"
                    }
                    if (re != "") s3 = s2.replaceFirst(re.toRegex(), "" + count)
                    count = 0
                } else if (s2[i] == '8') {
                    count++
                }
            }
            if (s3 == "") {
                s3 = "8"
            }
        } else {
            s3 = s1[q]
        }
        s1[q] = s3
    }
    s = ""
    for (s2 in s1) {
        s += "$s2/"
    }
    s = s.substring(0, s.length - 1) + " "
    s += "w "
    fun castleCheck(x: Int, y: Int) = board[x][y].piece?.moved == false
    var castle = ""
    if (castleCheck(0, 0) && castleCheck(0, 4)) castle += "K"
    if (castleCheck(0, 7) && castleCheck(0, 4)) castle += "Q"
    if (castleCheck(7, 0) && castleCheck(7, 4)) castle += "k"
    if (castleCheck(7, 7) && castleCheck(7, 4)) castle += "q"
    s += if (castle.isEmpty()) "- " else "$castle "
    s += if (lastMove == null) "- " else "$lastMove "
    s += moveCount
    return s
}
