import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.*;
import javax.swing.*;
 
public class MazeGenerator extends JPanel {
    enum Dir { //Tipe data bentukan
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0); //Titik x, y, dan z untuk setiap arah
        final int bit;
        final int dx;
        final int dy;
        Dir opposite;
 
        // menggunakan inisialisasi static untuk menyelesaikan masalah.
        static {
            N.opposite = S; //Opposite itu berhadapan
            S.opposite = N; //Jadi N.opposite = S berarti North (Utara) berhadapan dengan South (Selatan)
            E.opposite = W; //Ingat Gambar Arah aja
            W.opposite = E;
        }
 
        Dir(final int bit, final int dx, final int dy) {
            this.bit = bit;
            this.dx = dx;
            this.dy = dy;
        }
    };

    final int nCols;
    final int nRows;
    final int cellSize = 25; // Ukuran cell
    final int margin = 25; // Ukuran dinding
    final int[][] maze;
    LinkedList<Integer> solution;

    public MazeGenerator(final int size) {
        setPreferredSize(new Dimension(650, 650)); // Ukuran maze nya (x,y)
        setBackground(Color.white); // Set warna background menggunakan warna putih
        nCols = size;
        nRows = size;
        maze = new int[nRows][nCols];
        solution = new LinkedList<>();
        generateMaze(0, 0);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                new Thread(() -> {
                    solve(0);
                }).start();
            }
        });
    }

    @Override
    public void paintComponent(final Graphics gg) {
        super.paintComponent(gg);
        final Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setStroke(new BasicStroke(5));
        g.setColor(Color.black); // Set warna untuk dinding maze nya

        // draw maze
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {

                final int x = margin + c * cellSize;
                final int y = margin + r * cellSize;

                if ((maze[r][c] & 1) == 0) // Gambar dinding maze bagian N (North/Utara)
                    g.drawLine(x, y, x + cellSize, y);

                if ((maze[r][c] & 2) == 0) // Gambar dinding maze bagian N (South/Selatan)
                    g.drawLine(x, y + cellSize, x + cellSize, y + cellSize);

                if ((maze[r][c] & 4) == 0) // Gambar dinding maze bagian N (East/Timur)
                    g.drawLine(x + cellSize, y, x + cellSize, y + cellSize);

                if ((maze[r][c] & 8) == 0) // Gambar dinding maze bagian N (West/Barat)
                    g.drawLine(x, y, x, y + cellSize);
            }
        }

        // draw pathfinding animation
        final int offset = margin + cellSize / 2;

        final Path2D path = new Path2D.Float();
        path.moveTo(offset, offset);

        for (final int pos : solution) { // Pengulangan dari pos sampai menemukan solution (LinkedList)
            final int x = pos % nCols * cellSize + offset;
            final int y = pos / nCols * cellSize + offset;
            path.lineTo(x, y);
        }

        g.setColor(Color.orange); // Set warna orange untuk jalur yang dilalui dari titik start sampai titik
                                  // finish
        g.draw(path);

        g.setColor(Color.blue); // Set warna biru untuk titik start
        g.fillOval(offset - 5, offset - 5, 10, 10); // Membuat titik start yang telah di set warna biru

        g.setColor(Color.green); // Set warna hijau untuk titik finish
        final int x = offset + (nCols - 1) * cellSize; // Set letak titik hijau berdasarkan titik x
        final int y = offset + (nRows - 1) * cellSize; // Set letak titik hijau berdasarkan titik y
        g.fillOval(x - 5, y - 5, 10, 10); // Membuat titik finish yang telah di set warna hijau

    }

    void generateMaze(final int r, final int c) {
        final Dir[] dirs = Dir.values();
        Collections.shuffle(Arrays.asList(dirs));
        for (final Dir dir : dirs) {
            final int nc = c + dir.dx;
            final int nr = r + dir.dy;
            if (withinBounds(nr, nc) && maze[nr][nc] == 0) {
                // |= adalah operator penugasan
                maze[r][c] |= dir.bit; // maze[r][c] = maze[r][c] atau dir.bit
                maze[nr][nc] |= dir.opposite.bit; // maze[nr][nc] = maze[nr][nc] atau dir.opposite.bit
                generateMaze(nr, nc);
            }
        }
    }

    boolean withinBounds(final int r, final int c) {
        return c >= 0 && c < nCols && r >= 0 && r < nRows;
    }

    boolean solve(final int pos) {
        if (pos == nCols * nRows - 1)
            return true;

        final int c = pos % nCols;
        final int r = pos / nCols;

        for (final Dir dir : Dir.values()) {
            final int nc = c + dir.dx;
            final int nr = r + dir.dy;
            if (withinBounds(nr, nc) && (maze[r][c] & dir.bit) != 0 && (maze[nr][nc] & 16) == 0) {

                final int newPos = nr * nCols + nc;

                solution.add(newPos);
                maze[nr][nc] |= 16;

                animate();

                if (solve(newPos))
                    return true;

                animate();

                solution.removeLast();
                maze[nr][nc] &= ~16;

            }
        }

        return false;
    }

    void animate() {
        try {
            Thread.sleep(50); // Mengatur thread untuk menghentikan prosesnya sejenak dan memberi kesempatan
                              // pada thread atau proses lain, tipe data (ms)
        } catch (final InterruptedException ignored) {
        }
        repaint();
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Maze Generator");
            f.setResizable(false);
            f.add(new MazeGenerator(24), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}