package board_games.chess

private fun abs(a: Int) = if (a < 0) -a else a

enum class Color { BLACK, WHITE }

sealed class Piece(val color: Color) : Comparable<Piece> {
    internal open fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean = false
    var moved = false
        internal set
    val value: Int
        get() = when (this) {
            is King -> 10
            is Queen -> 9
            is Rook -> 5
            is Bishop -> 3
            is Knight -> 3
            is Pawn -> 1
        }

    val symbol: String
        get() = when (this) {
            is Pawn -> "P"
            is Rook -> "R"
            is Knight -> "N"
            is Bishop -> "B"
            is Queen -> "Q"
            is King -> "K"
        }

    val unicodeSymbol: String
        get() = when (this) {
            is Pawn -> if (color == Color.WHITE) '\u2659' else '\u265F'
            is Rook -> if (color == Color.WHITE) '\u2656' else '\u265C'
            is Knight -> if (color == Color.WHITE) '\u2658' else '\u265E'
            is Bishop -> if (color == Color.WHITE) '\u2657' else '\u265D'
            is Queen -> if (color == Color.WHITE) '\u2655' else '\u265B'
            is King -> if (color == Color.WHITE) '\u2654' else '\u265A'
        }.toString()

    override fun equals(other: Any?): Boolean = if (other is Piece) color == other.color else false
    override operator fun compareTo(other: Piece) = if (color == other.color) 0 else -1

    override fun toString(): String = "$color $symbol"

    protected fun lineEmpty(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        val (fromRow, fromCol) = fromCell
        val (toRow, toCol) = toCell
        // Attempt to move to the same cell
        if (fromRow == toRow && fromCol == toCol) return false
        // Collision detection
        when {
            fromRow == toRow -> { // Horizontal move
                val dx = if (fromCol < toCol) 1 else -1
                var i = fromCol + dx
                while (i != toCol) {
                    if (!board[fromRow][i].isEmpty()) return false
                    i += dx
                }
            }
            fromCol == toCol -> { // Vertical move
                val dy = if (fromRow < toRow) 1 else -1
                var i = fromRow + dy
                while (i != toRow) {
                    if (!board[i][fromCol].isEmpty()) return false
                    i += dy
                }
            }
            else -> return false // Not a valid rook move
        }
        return toCell.piece != fromCell.piece
    }

    protected fun diagonalEmpty(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        val (fromRow, fromCol) = fromCell
        val (rowTo, columnTo) = toCell
        if (fromRow == rowTo || fromCol == columnTo) return false //Did not move diagonally
        if (abs(rowTo - fromRow) != abs(columnTo - fromCol)) return false
        val rowOffset: Int = if (fromRow < rowTo) 1 else -1
        val colOffset: Int = if (fromCol < columnTo) 1 else -1
        var y = fromCol + colOffset
        var x = fromRow + rowOffset
        while (x != rowTo) {
            if (!board[x][y].isEmpty()) return false
            y += colOffset
            x += rowOffset
        }
        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + value
        result = 31 * result + unicodeSymbol.hashCode()
        return result
    }
}

class Pawn(color: Color) : Piece(color) {
    private var ep = false
    internal var enPassant: (Cell) -> Unit = {}
    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        //TODO: fix being able to go backwards
        var moves = false
        val (fromRow, fromCol) = fromCell
        val (rowTo, columnTo) = toCell
        //moving
        if (toCell.isEmpty()) {
            //moving up two
            if (!moved && abs(fromRow - rowTo) == 2 && fromCol == columnTo) {
                moves = true
                ep = true
                //moving up one
            } else if ((fromRow + 1 == rowTo || fromRow - 1 == rowTo) && fromCol == columnTo) {
                moves = true
                ep = false
                //en passant
            } else if ((fromRow + 1 == rowTo || fromCol + 1 == columnTo) &&
                (fromRow - 1 == rowTo || fromCol - 1 == columnTo) &&
                (board[fromRow][columnTo].piece as? Pawn)?.ep == true
            ) {
                moves = true
                ep = false
                enPassant(board[fromRow][columnTo])
            } else if ((fromRow + 1 == rowTo || fromCol - 1 == columnTo) &&
                (fromRow - 1 == rowTo || fromCol + 1 == columnTo) &&
                (board[fromRow][columnTo].piece as? Pawn)?.ep == true
            ) {
                moves = true
                ep = false
                enPassant(board[fromRow][columnTo])
            }
            //capturing
        } else if (!toCell.isEmpty()) {
            if (this != toCell.piece) {
                if ((fromRow + 1 == rowTo || fromCol + 1 == columnTo) && (fromRow - 1 == rowTo || fromCol - 1 == columnTo)) {
                    moves = true
                    ep = false
                } else if ((fromRow + 1 == rowTo || fromCol - 1 == columnTo) && (fromRow - 1 == rowTo || fromCol + 1 == columnTo)) {
                    moves = true
                    ep = false
                }
            }
        }
        if (moves) moved = true
        return moves
    }

}

class Rook(color: Color) : Piece(color) {
    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        var moves = false
        val isEmptyCheck = lineEmpty(board, fromCell, toCell)
        if (toCell.isEmpty() && isEmptyCheck) moves = true //moving
        else if (!toCell.isEmpty() && isEmptyCheck) {
            //capturing
            if (this != toCell.piece) moves = true
        }
        if (moves) moved = moves
        return moved
    }

}

class Knight(color: Color) : Piece(color) {
    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        var moves = false
        val (fromRow, fromCol) = fromCell
        val (rowTo, columnTo) = toCell
        if (abs(rowTo - fromRow) == 2 && abs(columnTo - fromCol) == 1) {
            if (toCell.isEmpty()) {
                moves = true
            } else if (this != toCell.piece) {
                moves = true
            }
        }

        if (abs(rowTo - fromRow) == 1 && abs(columnTo - fromCol) == 2) {
            if (toCell.isEmpty()) {
                moves = true
            } else if (this != toCell.piece) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class Bishop(color: Color) : Piece(color) {
    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        var moves = false
        if (toCell.isEmpty() && diagonalEmpty(board, fromCell, toCell)) {
            moves = true
            //capturing
        } else if (!toCell.isEmpty() && diagonalEmpty(board, fromCell, toCell)) {
            if (this != toCell.piece) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class Queen(color: Color) : Piece(color) {
    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        var moves = false
        if (toCell.isEmpty() && (diagonalEmpty(board, fromCell, toCell) || lineEmpty(board, fromCell, toCell))) {
            moves = true
            //capturing
        } else if (!toCell.isEmpty() && (diagonalEmpty(board, fromCell, toCell) || lineEmpty(
                board,
                fromCell,
                toCell
            ))
        ) {
            if (this != toCell.piece) {
                moves = true
            }
        }
        if (moves) moved = true
        return moves
    }
}

class King(color: Color) : Piece(color) {
    private fun checkForCheck(board: Board, kingToCell: Cell, kingColor: Color): Boolean {
        checkingLoop@ for (x in 0..7) {
            for (y in 0..7) {
                if (x == kingToCell.x && y == kingToCell.y) continue
                val cell = board[x][y]
                if (cell.piece is King || cell.piece == null) continue
                val colorCheck = cell.piece!!.color == kingColor
                if (cell.piece is Rook && colorCheck) continue
                if (
                    cell.piece!!.color != kingToCell.piece?.color &&
                    cell.piece!!.canMove(board, cell, kingToCell)
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun isAllTrueCheck(board: Board, kingColor: Color, rowTo: Int, fromCol: Int, vararg index: Int) =
        listOf(*index.toTypedArray()).mapNotNull {
            try {
                checkForCheck(board, board[rowTo][fromCol + it], kingColor)
            } catch (e: Exception) {
                null
            }
        }.all { it }

    override fun canMove(board: Board, fromCell: Cell, toCell: Cell): Boolean {
        var moves = false
        val (rowTo, columnTo) = toCell
        val (fromRow, fromCol) = fromCell
        if (!moved && columnTo - fromCol == 2 && fromRow == rowTo) {
            //kingside
            if (board[rowTo][fromCol + 1].isEmpty() &&
                board[rowTo][fromCol + 2].isEmpty() &&
                board[rowTo][fromCol + 3].piece?.moved == false &&
                isAllTrueCheck(board, color, rowTo, fromCol, 0, 1, 2)
            ) {
                moves = true
                board[rowTo][fromCol + 3].piece?.moved = true
                board[rowTo][fromCol + 1].piece = board[rowTo][fromCol + 3].piece
                board[rowTo][fromCol + 3].piece = null
            }
        } else if (!moved && fromCol - columnTo == 3 && fromRow == rowTo) {
            //queenside
            if (board[rowTo][fromCol - 1].isEmpty() &&
                board[rowTo][fromCol - 2].isEmpty() &&
                board[rowTo][fromCol - 3].isEmpty() &&
                board[rowTo][fromCol - 4].piece?.moved == false &&
                isAllTrueCheck(board, color, rowTo, fromCol, 0, -1, -2, -3)
            ) {
                moves = true
                board[rowTo][fromCol - 4].piece?.moved = true
                board[rowTo][fromCol - 2].piece = board[rowTo][fromCol - 4].piece
                board[rowTo][fromCol - 4].piece = null
            }
            //moving
        } else if (toCell.isEmpty()) {
            if (fromRow + 1 == rowTo || fromCol + 1 == columnTo || fromRow - 1 == rowTo || fromCol - 1 == columnTo) {
                moves = true
            }
            //capturing
        } else if (!toCell.isEmpty()) {
            if (this != toCell.piece) {
                if (fromRow + 1 == rowTo || fromCol + 1 == columnTo || fromRow - 1 == rowTo || fromCol - 1 == columnTo) {
                    moves = true
                }
            }
        }
        if (moves) moved = true
        return moves
    }
}
