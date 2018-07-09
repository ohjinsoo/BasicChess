package Pieces;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.lang.Math.abs;

public class Rook extends Piece {
    private BufferedImage rook;

    public Rook(String pos, int x, int y, boolean isWhite) {
        super(pos, x, y, isWhite);
        String imageSrc = "/";
        if (isWhite)
            imageSrc += "WhiteRook.png";
        else
            imageSrc += "BlackRook.png";

        try {
            rook = ImageIO.read(getClass().getResourceAsStream(imageSrc));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(rook, x, y, null);
    }

    @Override
    public boolean moveIsValid(String newPos, boolean attacking) {
        char newX = newPos.charAt(0);
        char xchar = pos.charAt(0);
        int newY = Integer.parseInt(newPos.substring(1));
        int yint = Integer.parseInt(pos.substring(1));

        int xdiff = abs(newX - xchar);
        int ydiff = abs(newY - yint);

        return xdiff == 0 || ydiff == 0;
    }
}