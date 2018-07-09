package Pieces;

import java.awt.*;

public abstract class Piece {
    protected String pos;
    protected int x, y;
    protected boolean isWhite;

    public Piece(String pos, int x, int y, boolean isWhite) {
        this.pos = pos;
        this.x = x;
        this.y = y;
        this.isWhite = isWhite;
    }

    public String getPos() {
        return pos;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }

    public abstract void paint(Graphics g);
    public abstract boolean moveIsValid(String newPos, boolean attacking);
}
