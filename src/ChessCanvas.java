import Pieces.*;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import static java.lang.Math.abs;
import static java.lang.Math.atan;

public class ChessCanvas  extends Canvas implements MouseListener {
    private JFrame frame;
    private BufferedImage board, blackText, whiteText, turn;
    private int turnX = 8, turnY = 21;
    private static final int WIDTH = 994, HEIGHT = 611
                            , INNER_BOUND_X = 242, INNER_BOUND_Y = 48
                            , OUTER_BOUND_X = 778, OUTER_BOUND_Y = 600
                            , PIECE_WIDTH = 67, PIECE_HEIGHT = 69;

    private HashMap<Character, Integer> xPos;
    private HashMap<Integer, Integer> yPos;
    private HashMap<String, Piece> piecesMap;

    private Piece[] black = new Piece[16];
    private Piece[] white = new Piece[16];

    private int[] whiteDeadX = {20, 87};
    private int[] blackDeadX = {830, 897};
    private int[] deadY = {58, 127, 196, 265, 334, 403, 472, 541};

    private int blackDead = 0;
    private int whiteDead = 0;

    private boolean isGameOver = false;
    private boolean whiteWon = false;
    private boolean restartGame = false;

    private boolean isWhiteTurn = true;
    private boolean attackMode = false;
    private Piece attacker;
    private Pair enpassantPair = null;

    public ChessCanvas(JFrame frame) {
        this.frame = frame;
        this.frame.addMouseListener(this);
        addMouseListener(this);
        this.init();
    }

    private void init() {
        xPos = new HashMap<>();
        yPos = new HashMap<>();
        piecesMap = new HashMap<>();

        int x = INNER_BOUND_X;
        for (char letter = 'A'; letter <= 'H'; letter++) {
            xPos.put(letter, x);
            x += PIECE_WIDTH;
        }

        int y = INNER_BOUND_Y;
        for (int i = 8; i > 0; i--) {
            yPos.put(i, y);
            y += PIECE_HEIGHT;
        }

        initPieces(black, false);
        initPieces(white, true);

        try {
            board = ImageIO.read(getClass().getResourceAsStream("/Board.png"));
            blackText = ImageIO.read(getClass().getResourceAsStream("/BlackPieces.png"));
            whiteText = ImageIO.read(getClass().getResourceAsStream("/WhitePieces.png"));
            turn = ImageIO.read(getClass().getResourceAsStream("/Turn.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setFocusable(false);
        setIgnoreRepaint(true);
        this.setSize(WIDTH, HEIGHT);
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
                System.exit(0);
            }
        });
    }

    private void initPieces(Piece[] pieces, boolean isWhite) {
        int pawnY;
        if (isWhite)
            pawnY = 2;
        else
            pawnY = 7;

        // Initialize the PAWNS.
        char letter = 'A';
        for (int i = 0; i < 8; i++) {
            String pos = letter + Integer.toString(pawnY);
            pieces[i] = new Pawn(pos, xPos.get(letter), yPos.get(pawnY), isWhite);
            piecesMap.put(pos, pieces[i]);
            letter++;
        }

        if (isWhite)
            pawnY = 1;
        else
            pawnY = 8;

        // Initialize the ROOKS.
        pieces[8] = new Rook("A" + pawnY, xPos.get('A'), yPos.get(pawnY), isWhite);
        pieces[9] = new Rook("H" + pawnY, xPos.get('H'), yPos.get(pawnY), isWhite);
        piecesMap.put("A" + pawnY, pieces[8]);
        piecesMap.put("H" + pawnY, pieces[9]);

        // Initialize the KNIGHTS.
        pieces[10] = new Knight("B" + pawnY, xPos.get('B'), yPos.get(pawnY), isWhite);
        pieces[11] = new Knight("G" + pawnY, xPos.get('G'), yPos.get(pawnY), isWhite);
        piecesMap.put("B" + pawnY, pieces[10]);
        piecesMap.put("G" + pawnY, pieces[11]);

        // Initialize the BISHOPS.
        pieces[12] = new Bishop("C" + pawnY, xPos.get('C'), yPos.get(pawnY), isWhite);
        pieces[13] = new Bishop("F" + pawnY, xPos.get('F'), yPos.get(pawnY), isWhite);
        piecesMap.put("C" + pawnY, pieces[12]);
        piecesMap.put("F" + pawnY, pieces[13]);

        // Initialize the KING / QUEEN.
        pieces[14] = new Queen("D" + pawnY, xPos.get('D'), yPos.get(pawnY), isWhite);
        pieces[15] = new King("E" + pawnY, xPos.get('E'), yPos.get(pawnY), isWhite);
        piecesMap.put("D" + pawnY, pieces[14]);
        piecesMap.put("E" + pawnY, pieces[15]);


    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(board, 0, 0, null);
        g.drawImage(turn, turnX, turnY, null);
        g.drawImage(blackText, 814, 28, null);
        g.drawImage(whiteText,12, 28, null);

        for (int i = 0; i < 16; i++) {
            black[i].paint(g);
            white[i].paint(g);

            // Reset en passant positions for the appropriate color.
            if (isWhiteTurn)
                white[i].setEnpassantPos("");
            else
                black[i].setEnpassantPos("");
        }

        if (isGameOver) {
            String endGameImageSrc = "/";
            int endX = 15;
            int endY = 100;
            if (whiteWon)
                endGameImageSrc += "White";
            else {
                endGameImageSrc += "Black";
                endX = 815;
            }
            endGameImageSrc += "Wins.png";
            try {
                BufferedImage endGame = ImageIO.read(getClass().getResourceAsStream(endGameImageSrc));
                g.drawImage(endGame, endX, endY, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        When mouse is clicked in bounds of the board AND clicks on a valid piece, update.
     */
    private void choosePieceToUpdate(String pos) {
        attacker = piecesMap.get(pos);
        attackMode = true;
        this.repaint();
    }

    /*
        Choose where you want your piece to move/attack to.
     */
    private void move(String pos) {
        if (piecesMap.containsKey(pos)) {
            removePieceFromBoard(pos);
        }
        else if (enpassantPair != null && enpassantPair.getKey().equals(pos)) {
            removePieceFromBoard((String)enpassantPair.getValue());
        }

        piecesMap.remove(attacker.getPos());
        piecesMap.put(pos, attacker);

        attacker.setPos(pos);
        attacker.setXY(xPos.get(pos.charAt(0)), yPos.get(Integer.parseInt((pos.substring(1)))));

        int promotionY = 1;
        if (attacker.isWhite())
            promotionY = 8;

        if (attacker instanceof Pawn && Integer.parseInt(pos.substring(1)) == promotionY) {
            promotePawn();
        }

        if (!attacker.getEnpassantPos().equals(""))
            enpassantPair = new Pair<>(attacker.getEnpassantPos(), attacker.getPos());
        else
            enpassantPair = null;

        attackMode = false;
        attacker = null;

        if (isWhiteTurn)
            turnX = 806;
        else
            turnX = 8;

        isWhiteTurn = !isWhiteTurn;
        this.repaint();
    }

    private void removePieceFromBoard(String pos) {
        Piece deadPiece = piecesMap.get(pos);
        String deadPos = "DEAD";
        int dX = 0;
        int dY = 0;

        if (isWhiteTurn) {
            deadPos += blackDead;
            dX = blackDeadX[blackDead % 2];
            dY = deadY[blackDead / 2];
            blackDead++;
        }
        else {
            deadPos += whiteDead;
            dX = whiteDeadX[whiteDead % 2];
            dY = deadY[whiteDead / 2];
            whiteDead++;
        }
        deadPiece.setPos(deadPos);
        deadPiece.setXY(dX, dY);

        if (deadPiece instanceof King) {
            isGameOver = true;
            whiteWon = !deadPiece.isWhite();
        }
    }

    private void promotePawn() {
        String[] possiblePieces = { "Queen", "Rook", "Bishop", "Knight", "Pawn" };
        Object selectedPiece = JOptionPane.showInputDialog(null,
                "Choose which class to promote your pawn to.", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                possiblePieces, possiblePieces[0]);

        if (selectedPiece == "Queen") {
            System.out.println("happened!!!");
            attacker = new Queen(attacker.getPos(), attacker.getX(), attacker.getY(), attacker.isWhite());
        }
        else if (selectedPiece == "Rook")
            attacker = new Rook(attacker.getPos(), attacker.getX(), attacker.getY(), attacker.isWhite());
        else if (selectedPiece == "Bishop")
            attacker = new Bishop(attacker.getPos(), attacker.getX(), attacker.getY(), attacker.isWhite());
        else if (selectedPiece == "Knight")
            attacker = new Knight(attacker.getPos(), attacker.getX(), attacker.getY(), attacker.isWhite());
        else if (selectedPiece == "Pawn")
            attacker = new Pawn(attacker.getPos(), attacker.getX(), attacker.getY(), attacker.isWhite());

        for (int i = 0; i < 8; i++) {
            if (black[i].getPos().equals(attacker.getPos())) {
                black[i] = attacker;
                piecesMap.put(attacker.getPos(), attacker);
                break;
            }
            else if (white[i].getPos().equals(attacker.getPos())) {
                white[i] = attacker;
                piecesMap.put(attacker.getPos(), attacker);
                break;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isGameOver) {
            restartGame = true;
            return;
        }
        System.out.println(enpassantPair);
        int x = e.getX();
        int y = e.getY();

        int tempX = (x - INNER_BOUND_X) / PIECE_WIDTH;
        char boardX = 'A';
        int boardY = (y - INNER_BOUND_Y) / PIECE_HEIGHT;

        boardX += tempX;
        String pos = boardX + Integer.toString(8 - boardY);

        // Check if in bounds.
        if (x >= INNER_BOUND_X && y >= INNER_BOUND_Y && x <= OUTER_BOUND_X && y <= OUTER_BOUND_Y) {
            // If a piece was already clicked on previously, it is now attack mode. Choose where to send your piece.
            // Make sure the move is valid.
            if (attackMode && (!piecesMap.containsKey(pos) || piecesMap.get(pos).isWhite() != isWhiteTurn)) {
                boolean attacking = piecesMap.containsKey(pos);
                if (enpassantPair != null)
                    attacking = attacking || enpassantPair.getKey().equals(pos);

                if (attacker.moveIsValid(pos, attacking)) {
                    //Check for any collision IF you're not a horse or King.
                    boolean collision = false;
                    if (attacker instanceof Queen || attacker instanceof Bishop)
                        collision = diagonalCollision(pos);
                    if ((attacker instanceof Queen || attacker instanceof Rook || attacker instanceof Pawn) && !collision)
                        collision = straightCollision(pos);

                    if (!collision)
                        move(pos);
                }
            }

            // Else, not in attack mode. Choose a piece that is yours to move.
            else if (piecesMap.containsKey(pos) && piecesMap.get(pos).isWhite() == isWhiteTurn)
                choosePieceToUpdate(pos);
        }
    }

    private boolean diagonalCollision(String pos) {
        char xPos = pos.charAt(0);
        char xAttack = attacker.getPos().charAt(0);
        int yPos = Integer.parseInt(pos.substring(1));
        int yAttack = Integer.parseInt(attacker.getPos().substring(1));
        int xdiff = abs(xPos - xAttack);
        int ydiff = abs(yPos - yAttack);

        // Check to make sure it is an actual diagonal move.
        if (xdiff != ydiff)
            return false;

        int xRate = 1;
        if (xAttack < xPos)
            xRate = -1;

        int yRate = 1;
        if (yAttack < yPos)
            yRate = -1;

        char inc = xPos;
        inc += xRate;

        for (int i = 0; i < xdiff - 1; i ++) {
            yPos += yRate;
            String tempPos = inc + Integer.toString(yPos);
            System.out.println("checking for diag. collision at: " + tempPos);
            if (piecesMap.containsKey(tempPos)) {
                System.out.println("COLLISION DETECTED AT: " + tempPos);
                return true;
            }
            inc += xRate;
        }

        return false;
    }

    private boolean straightCollision(String pos) {
        char xPos = pos.charAt(0);
        char xAttack = attacker.getPos().charAt(0);
        int yPos = Integer.parseInt(pos.substring(1));
        int yAttack = Integer.parseInt(attacker.getPos().substring(1));

        int xdiff = abs(xPos - xAttack);
        int ydiff = abs(yPos - yAttack);

        // Check to make sure it is an actual straight move.
        if (xdiff == ydiff)
            return false;

        if (xdiff != 0) {
            char lower = xAttack;
            char upper = xPos;
            if (xPos < xAttack) {
                lower = xPos;
                upper = xAttack;
            }
            lower++;
            for (char i = lower; i < upper; i++) {
                String tempPos = i + Integer.toString(yPos);
                System.out.println("checking for straight collision at: " + tempPos);
                if (piecesMap.containsKey(tempPos)) {
                    System.out.println("COLLISION DETECTED AT: " + tempPos);
                    return true;
                }
            }
        }
        else {
            int lower = yAttack;
            int upper = yPos;
            if (yPos < yAttack) {
                lower = yPos;
                upper = yAttack;
            }
            lower++;
            for (int i = lower; i < upper; i++) {
                String tempPos = xPos + Integer.toString(i);
                System.out.println("checking for straight collision at: " + tempPos);
                if (piecesMap.containsKey(tempPos)) {
                    System.out.println("COLLISION DETECTED AT: " + tempPos);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public boolean restartGame() {
        return restartGame;
    }
}
