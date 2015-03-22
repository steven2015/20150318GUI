/**
 *
 */
package steven.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import javax.swing.JFrame;

/**
 * @author Steven
 *
 */
public class FixedSizeWindow{
	private final JFrame jframe;
	private final Runnable uiRunnable;
	private final long uiUpdateInterval;
	private volatile Image buffer;
	private boolean uiActive;

	/**
	 * @param width
	 * @param height
	 */
	public FixedSizeWindow(final int width, final int height){
		this.jframe = new JFrame(){
			private static final long serialVersionUID = 8001680304095738552L;

			@Override
			public void paint(final Graphics g){
				if(FixedSizeWindow.this.buffer == null){
					FixedSizeWindow.this.buffer = FixedSizeWindow.this.jframe.createImage(FixedSizeWindow.this.jframe.getWidth(), FixedSizeWindow.this.jframe.getHeight());
				}
				final Graphics2D g2d = (Graphics2D)FixedSizeWindow.this.buffer.getGraphics();
				g2d.setFont(new Font("MingLiU", Font.PLAIN, 24));
				g2d.drawString("1234567890", 100, 100);
				g2d.drawString("abcijklmno", 100, 130);
				g2d.drawString("一二三四五", 100, 160);
				final int[] fontSizes = new int[]{12, 16, 20, 24};
				for(final Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()){
					for(final int fontSize : fontSizes){
						final Font newFont = font.deriveFont((float)fontSize);
						final FontMetrics fm = g2d.getFontMetrics(newFont);
						final int w1 = fm.charWidth('l');
						final int w2 = fm.charWidth('m');
						final int w3 = fm.charWidth('一');
						if(w1 == w2 && w1 * 2 == w3){
							System.out.println(font + " " + fontSize + " " + w1 + " " + fm.getHeight() + " " + newFont.canDisplay('l') + " " + newFont.canDisplay('m') + " " + newFont.canDisplay('一'));
						}
					}
				}
				System.out.println();
				FixedSizeWindow.this.uiActive = false;
				g2d.dispose();
				g.drawImage(FixedSizeWindow.this.buffer, 0, 0, width, height, null);
			}
		};
		this.jframe.setUndecorated(true);
		this.jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.jframe.setSize(width, height);
		this.jframe.setFocusTraversalKeysEnabled(true);
		this.jframe.setResizable(false);
		this.uiUpdateInterval = 1000 / 30;
		this.uiRunnable = () -> {
			while(true){
				if(this.uiActive){
					this.jframe.repaint();
					try{
						Thread.sleep(this.uiUpdateInterval);
					}catch(final InterruptedException e){
					}
				}else{
					try{
						Thread.sleep(Long.MAX_VALUE);
					}catch(final InterruptedException e){
					}
				}
			}
		};
		this.jframe.setVisible(true);
		new Thread(this.uiRunnable).start();
		for(final Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()){
			System.out.println(font);
		}
	}
	public void setUiActive(final boolean uiActive){
		this.uiActive = uiActive;
	}
	public static final void main(final String[] args){
		new FixedSizeWindow(800, 600).setUiActive(true);
	}
}
