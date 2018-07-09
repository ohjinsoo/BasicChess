import javax.swing.*;
import java.awt.event.ActionEvent;

public class BasicChessEngine {
    private JFrame frame;
    private ChessCanvas canvas;

    public BasicChessEngine() {
        this.frame = new JFrame("BasicChess");
        init();
    }

    private void init() {
        frame.getContentPane().removeAll();
        frame.repaint();
        canvas = new ChessCanvas(frame);
        frame.setResizable(false);
        JPanel panel = new JPanel(true);
        panel.add(canvas);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        canvas.createBufferStrategy(2);
    }
    public void run() {
        // Initial paint.
        Timer timer = new Timer(20 , this::update);
        timer.setRepeats(true);
        timer.start();
        canvas.repaint();
    }

    private void update(ActionEvent e) {
        boolean restart = canvas.restartGame();
        if (restart) {
            init();
            run();
        }
    }
}
