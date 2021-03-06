package br.ufes.inf.nemo.oled;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import br.ufes.inf.nemo.oled.ui.StatusListener;


public class AppStatusBar extends JPanel implements StatusListener{

	private static final long serialVersionUID = -1470943434794934781L;
	private JLabel statusLabel = new JLabel();
	private JLabel barTextLabel = new JLabel();
	//private JLabel coordLabel = new JLabel("    ");
	//private JLabel memLabel = new JLabel("    ");
	private JProgressBar memBar = new JProgressBar();
	private transient Timer timer = new Timer();	

	public Timer getTimer() {
		return timer;
	}

	public void clearStatus()
	{
		statusLabel.setText("");
	}
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public AppStatusBar()
	{
		super(new BorderLayout());
		
		setBorder(new EmptyBorder(3, 3, 3, 3));
		
		add(statusLabel, BorderLayout.CENTER);
		setPreferredSize(new Dimension(450,36));
		
		JPanel panel = new JPanel();
				
		memBar.setMinimum(0);
		memBar.setMaximum((int)Runtime.getRuntime().totalMemory());
		memBar.setValue((int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		memBar.setStringPainted(true);
		memBar.setToolTipText(getMemString());	
		barTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		barTextLabel.setText("Memory Usage:    ");
		
		memBar.setSize(new Dimension(50, 20));
		add(panel, BorderLayout.EAST);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(barTextLabel, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(memBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(5)
							.addComponent(memBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(6)
							.addComponent(barTextLabel)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
						
		scheduleMemTimer();
		
	}

	/**
	 * Shows the inputed text in the status bar
	 * @param the text 
	 */
	public void reportStatus(String status)
	{
		statusLabel.setText(status);
	}
	
	/**
	 * Sets up and starts the timer task.
	 */
	private void scheduleMemTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						memBar.setMinimum(0);
						memBar.setMaximum((int)Runtime.getRuntime().totalMemory());
						memBar.setValue((int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
						memBar.setStringPainted(true);
						memBar.setToolTipText(getMemString());	
						barTextLabel.setText("Memory Usage:");
						//memLabel.setText(getMemString());
					}
				});
			}
		};
		// every 5 seconds
		timer.schedule(task, 2000, 5000);
	}
	
	/**
	 * Creates the memory information string.
	 * @return the memory status string
	 */
	
	private String getMemString() {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long used = total - free;
		used /= (1024 * 1024);
		total /= (1024 * 1024);
		return String.format("Used: %dM Total: %dM   ", used, total);
	}
	
}
