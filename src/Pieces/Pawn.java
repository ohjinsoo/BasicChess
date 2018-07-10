package Pieces;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.lang.Math.abs;

public class Pawn extends Piece {
    private BufferedImage pawn;
    private boolean movedBefore = false;

    public Pawn(String pos, int x, int y, boolean isWhite) {
        super(pos, x, y, isWhite);
        String imageSrc = "/";
        if (isWhite)
            imageSrc += "WhitePawn.png";
        else
            imageSrc += "BlackPawn.png";

        try {
            pawn = ImageIO.read(getClass().getResourceAsStream(imageSrc));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(pawn, x, y, null);
    }

    @Override
    public boolean moveIsValid(String newPos, boolean attacking) {
        char newX = newPos.charAt(0);
        char xchar = pos.charAt(0);
        int newY = Integer.parseInt(newPos.substring(1));
        int yint = Integer.parseInt(pos.substring(1));

        if (attacking) {
            int xdiff = abs(newX - xchar);
            int ydiff = yint - newY;

            if (isWhite)
                ydiff = newY - yint;
            boolean ret = (xdiff == 1 && ydiff == 1);
            if (ret)
                movedBefore = true;

            return ret;
        }
        else {
            int diffLimit = 1;
            if (!movedBefore)
                diffLimit = 2;

            int lower = newY;
            int upper = yint;

            if (isWhite) {
                lower = yint;
                upper = newY;
            }

            System.out.println("upper: " + upper);
            System.out.println("lower: " + lower);
            System.out.println("diffLimit: " + diffLimit);

            boolean ret = (newX == xchar) && (upper - lower <= diffLimit) && (upper - lower >= 0);
            System.out.println(ret);
            if (ret)
                movedBefore = true;

            return ret;
        }
    }
}