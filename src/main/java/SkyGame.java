import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

public class SkyGame extends JFrame {
    private static final int W = 900, H = 600;
    private final CardLayout layout = new CardLayout();
    private final JPanel screens = new JPanel(layout);
    private final MenuPanel menu = new MenuPanel();
    private final GamePanel game = new GamePanel();
    private final Preferences prefs = Preferences.userNodeForPackage(SkyGame.class);
    private int best = 0;

    public SkyGame() {
        best = prefs.getInt("bestScore", 0);
        setTitle("SKY GLIDER - Endless Flight");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        screens.add(menu, "MENU");
        screens.add(game, "GAME");
        setContentPane(screens);
        setSize(W, H);
        setLocationRelativeTo(null);
    }

    private void startGame() {
        game.resetGame();
        layout.show(screens, "GAME");
        game.requestFocusInWindow();
    }
    private void showMenu() {
        game.stopGame();
        menu.updateBest();
        layout.show(screens, "MENU");
    }
    private void saveBest() {
        if (game.score > best) {
            best = game.score;
            prefs.putInt("bestScore", best);
            menu.updateBest();
        }
    }
    private JButton button(String label) {
        JButton b = new JButton(label);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(20, 105, 180));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(130,220,255), 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private class MenuPanel extends JPanel {
        private final JLabel bestLabel = new JLabel();
        MenuPanel() {
            setLayout(null);
            bestLabel.setFont(new Font("Arial", Font.BOLD, 20));
            bestLabel.setForeground(new Color(255,220,80));
            bestLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(bestLabel);
            JButton play = button("PLAY GAME");
            JButton how = button("INSTRUCTIONS");
            JButton exit = button("EXIT");
            add(play); add(how); add(exit);
            play.addActionListener(e -> startGame());
            how.addActionListener(e -> JOptionPane.showMessageDialog(SkyGame.this,
                    "HOW TO PLAY\\n\\nArrow keys: move the plane\\nAvoid red enemy planes\\nCollect gold coins\\nYou have 3 lives.\\n\\nOn game over, click PLAY AGAIN or MAIN MENU.",
                    "Instructions", JOptionPane.INFORMATION_MESSAGE));
            exit.addActionListener(e -> System.exit(0));
            updateBest();
        }
        void updateBest() { bestLabel.setText("BEST SCORE: " + best); }
        @Override public void doLayout() {
            int cx = getWidth()/2;
            bestLabel.setBounds(cx-150, 245, 300, 35);
            Component[] c = getComponents();
            // child 1 play, 2 instructions, 3 exit
            c[1].setBounds(cx-130, 300, 260, 48);
            c[2].setBounds(cx-130, 365, 260, 48);
            c[3].setBounds(cx-130, 430, 260, 48);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D x=(Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            x.setPaint(new GradientPaint(0,0,new Color(5,20,55),0,getHeight(),new Color(15,125,175)));
            x.fillRect(0,0,getWidth(),getHeight());
            x.setColor(new Color(255,220,100,70)); x.fillOval(getWidth()-210,35,145,145);
            x.setColor(new Color(255,230,130)); x.fillOval(getWidth()-178,67,80,80);
            x.setFont(new Font("Arial",Font.BOLD,58)); x.setColor(Color.WHITE);
            FontMetrics fm=x.getFontMetrics(); String title="SKY GLIDER";
            x.drawString(title,(getWidth()-fm.stringWidth(title))/2,150);
            x.setFont(new Font("Arial",Font.BOLD,18)); x.setColor(new Color(130,230,255));
            String sub="ENDLESS FLIGHT"; fm=x.getFontMetrics();
            x.drawString(sub,(getWidth()-fm.stringWidth(sub))/2,190);
            x.setColor(new Color(55,100,135)); int[] a={0,130,250,400,540,700,900}; int[] b={520,370,500,350,480,375,510}; x.fillPolygon(a,b,a.length);
            x.setColor(new Color(15,55,80)); int[] c={0,180,350,530,720,900}; int[] d={550,440,535,425,535,460}; x.fillPolygon(c,d,c.length);
            x.dispose();
        }
    }

    private class GamePanel extends JPanel implements ActionListener {
        private final Timer timer = new Timer(16, this);
        private final Random rng = new Random();
        int planeX=100, planeY=280, enemyX=850, enemyY=220, coinX=700, coinY=350;
        int score=0, coins=0, lives=3, tick=0, speed=5, cloud=0;
        private final ArrayList<Bullet> bullets = new ArrayList<>();
        private int lastSpeedLevel = 0;
        boolean over=false;

        private class Bullet {
            int x, y;
            Bullet(int x, int y) { this.x=x; this.y=y; }
        }
        private final JButton again = button("PLAY AGAIN");
        private final JButton back = button("MAIN MENU");

        GamePanel() {
            setLayout(null);
            setFocusable(true);
            setBackground(new Color(225,240,248));
            add(again); add(back);
            again.setVisible(false); back.setVisible(false);
            again.addActionListener(e -> resetGame());
            back.addActionListener(e -> showMenu());
            bind("UP", KeyEvent.VK_UP, 0, -18);
            bind("DOWN", KeyEvent.VK_DOWN, 0, 18);
            bind("LEFT", KeyEvent.VK_LEFT, -18, 0);
            bind("RIGHT", KeyEvent.VK_RIGHT, 18, 0);
            bind("W", KeyEvent.VK_W, 0, -18);
            bind("S", KeyEvent.VK_S, 0, 18);
            bind("A", KeyEvent.VK_A, -18, 0);
            bind("D", KeyEvent.VK_D, 18, 0);
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0),"FIRE");
            getActionMap().put("FIRE", new AbstractAction(){public void actionPerformed(ActionEvent e){
                if(!over && bullets.size() < 8) bullets.add(new Bullet(planeX+78, planeY+15));
            }});
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,0),"RESTART");
            getActionMap().put("RESTART", new AbstractAction(){public void actionPerformed(ActionEvent e){if(over) resetGame();}});
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M,0),"MENU");
            getActionMap().put("MENU", new AbstractAction(){public void actionPerformed(ActionEvent e){if(over) showMenu();}});
            resetGame();
            timer.stop();
        }
        private void bind(String name, int key, int dx, int dy) {
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key,0),name);
            getActionMap().put(name,new AbstractAction(){public void actionPerformed(ActionEvent e){
                if(!over) { planeX=Math.max(10,Math.min(getWidth()-105,planeX+dx)); planeY=Math.max(95,Math.min(getHeight()-95,planeY+dy)); repaint(); }
            }});
        }
        @Override public void doLayout() {
            int cx=getWidth()/2;
            again.setBounds(cx-140, getHeight()/2+65, 130, 42);
            back.setBounds(cx+10, getHeight()/2+65, 130, 42);
        }
        void resetGame() {
            planeX=90; planeY=Math.max(150,getHeight()/2-25);
            enemyX=Math.max(getWidth(),850); enemyY=170+rng.nextInt(250);
            coinX=Math.max(getWidth()+220,700); coinY=140+rng.nextInt(300);
            score=0; coins=0; lives=3; tick=0; speed=5; cloud=0; over=false;
            bullets.clear(); lastSpeedLevel=0;
            again.setVisible(false); back.setVisible(false);
            timer.start(); repaint(); requestFocusInWindow();
        }
        void stopGame(){timer.stop();}
        @Override public void actionPerformed(ActionEvent e) {
            if(over) return;
            tick++; cloud=(cloud+2)%1000;
            if(tick%6==0) score++; // score also represents travel distance
            int speedLevel = score / 100;
            if(speedLevel > lastSpeedLevel) { speed = Math.min(11, 5 + speedLevel); lastSpeedLevel = speedLevel; }
            enemyX-=speed; coinX-=speed;
            for(Iterator<Bullet> it=bullets.iterator(); it.hasNext();) {
                Bullet b=it.next(); b.x += 13;
                if(b.x > getWidth()) it.remove();
            }
            if(enemyX < -100){enemyX=getWidth()+rng.nextInt(260);enemyY=130+rng.nextInt(Math.max(100,getHeight()-260));score+=5;}
            if(coinX < -35){coinX=getWidth()+180+rng.nextInt(300);coinY=120+rng.nextInt(Math.max(100,getHeight()-240));}
            Rectangle plane=new Rectangle(planeX+8,planeY+8,70,30);
            Rectangle enemy=new Rectangle(enemyX+5,enemyY+5,62,28);
            Rectangle coin=new Rectangle(coinX,coinY,28,28);
            for(Iterator<Bullet> it=bullets.iterator(); it.hasNext();) {
                Bullet b=it.next();
                if(new Rectangle(b.x, b.y, 22, 6).intersects(enemy)) {
                    it.remove(); enemyX=getWidth()+rng.nextInt(260);
                    enemyY=130+rng.nextInt(Math.max(100,getHeight()-260));
                    score += 20; break;
                }
            }
            if(plane.intersects(enemy)){lives--;enemyX=getWidth()+200;enemyY=130+rng.nextInt(Math.max(100,getHeight()-260));if(lives<=0)finish();}
            if(!over && plane.intersects(coin)){coins++;score+=10;coinX=getWidth()+220+rng.nextInt(300);coinY=120+rng.nextInt(Math.max(100,getHeight()-240));}
            saveBest(); repaint();
        }
        private void finish(){over=true;timer.stop();again.setVisible(true);back.setVisible(true);saveBest();revalidate();repaint();}
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D x=(Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            x.setPaint(new GradientPaint(0,0,new Color(235,245,250),0,h,new Color(180,220,235)));
            x.fillRect(0,0,w,h);
            x.setColor(new Color(255,255,255,180));
            for(int i=0;i<7;i++){int cx=(i*170-cloud%(w+200)+w+200)%(w+200)-80;int cy=100+(i%3)*55;x.fillOval(cx,cy,70,24);x.fillOval(cx+20,cy-12,45,35);x.fillOval(cx+45,cy,65,24);}
            x.setColor(new Color(72,130,155)); int[] mx={0,w/7,w/4,w/2,2*w/3,5*w/6,w};int[] my={h-70,h-190,h-90,h-205,h-100,h-175,h-65};x.fillPolygon(mx,my,mx.length);
            x.setColor(new Color(30,90,105)); int[] fx={0,w/5,w/3,w/2,2*w/3,5*w/6,w};int[] fy={h-25,h-100,h-45,h-115,h-40,h-95,h-20};x.fillPolygon(fx,fy,fx.length);
            drawPlane(x,planeX,planeY,false);
            drawPlane(x,enemyX,enemyY,true);
            // Bright projectiles fired by the player
            for(Bullet b : bullets) {
                x.setColor(new Color(255,95,25)); x.fillRoundRect(b.x,b.y,22,6,6,6);
                x.setColor(new Color(255,235,110)); x.fillRoundRect(b.x+3,b.y+1,14,3,3,3);
            }
            x.setColor(new Color(255,195,0));x.fillOval(coinX,coinY,28,28);x.setColor(new Color(255,245,150));x.drawOval(coinX+3,coinY+3,22,22);x.setColor(new Color(130,80,0));x.setFont(new Font("Arial",Font.BOLD,18));x.drawString("$",coinX+8,coinY+20);
            x.setColor(new Color(25,25,25,180));x.fillRoundRect(15,15,210,94,12,12);
            x.setColor(Color.WHITE);x.setFont(new Font("Arial",Font.BOLD,17));x.drawString("SCORE: "+score,28,40);x.drawString("COINS: "+coins,28,63);x.drawString("LIVES: "+lives,28,86);
            x.setFont(new Font("Arial",Font.BOLD,14));x.setColor(new Color(40,60,75));x.drawString("Move: ARROWS / WASD   FIRE: SPACE   SPEED: "+speed,Math.max(250,w-455),28);
            if(over){
                x.setColor(new Color(0,0,0,175));x.fillRect(0,0,w,h);
                x.setColor(Color.WHITE);x.setFont(new Font("Arial",Font.BOLD,54));drawCentered(x,"GAME OVER",h/2-45);
                x.setFont(new Font("Arial",Font.BOLD,23));x.setColor(new Color(255,215,80));drawCentered(x,"Final score: "+score+"     Best: "+best,h/2+5);
                x.setFont(new Font("Arial",Font.PLAIN,17));x.setColor(Color.WHITE);drawCentered(x,"Click PLAY AGAIN to restart or MAIN MENU to go back",h/2+42);
            }
            x.dispose();
        }
        private void drawCentered(Graphics2D g,String s,int y){FontMetrics fm=g.getFontMetrics();g.drawString(s,(getWidth()-fm.stringWidth(s))/2,y);}
        private void drawPlane(Graphics2D g,int x,int y,boolean enemy){
            Graphics2D p=(Graphics2D)g.create();p.translate(x,y);
            if(enemy)p.scale(-1,1);
            p.setColor(enemy?new Color(190,35,55):new Color(30,65,95));
            int[] bx={0,24,72,94,72,25};int[] by={18,10,0,18,28,25};p.fillPolygon(bx,by,bx.length);
            p.setColor(enemy?new Color(255,175,65):new Color(70,185,230));
            int[] wx={27,48,65,55,24};int[] wy={15,34,32,20,20};p.fillPolygon(wx,wy,wx.length);
            p.setColor(new Color(235,245,255));p.fillOval(48,8,18,9);
            p.setColor(new Color(255,130,20));p.fillPolygon(new int[]{-9,1,1},new int[]{18,13,23},3);
            p.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SkyGame().setVisible(true));
    }
}
