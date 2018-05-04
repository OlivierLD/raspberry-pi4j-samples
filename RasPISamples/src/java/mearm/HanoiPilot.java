package mearm;

import com.pi4j.io.i2c.I2CFactory;
import hanoitower.BackendAlgorithm;
import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;
import i2c.samples.mearm.MeArmPilot;

import javax.swing.JOptionPane;

import static i2c.samples.mearm.MeArmPilot.DEFAULT_BOTTOM_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_CLAW_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_LEFT_SERVO_CHANNEL;
import static i2c.samples.mearm.MeArmPilot.DEFAULT_RIGHT_SERVO_CHANNEL;

public class HanoiPilot {

	private static int nbDisc = 4;
	private static int nbMove = 0;

	private static int
			left = DEFAULT_LEFT_SERVO_CHANNEL,
			right = DEFAULT_RIGHT_SERVO_CHANNEL,
			bottom = DEFAULT_BOTTOM_SERVO_CHANNEL,
			claw = DEFAULT_CLAW_SERVO_CHANNEL;

	private static HanoiContext.Stand hanoiStand = null;
	private static Thread me;

	private static synchronized void startSolving() {
		System.out.println(String.format("Starting solving, anticipating %d moves.", (int)(Math.pow(2, nbDisc) - 1)));
		nbMove = 0;
		BackendAlgorithm.move(nbDisc, "A", "C", "B");
		System.out.println((new StringBuilder())
				.append("Finished in ")
				.append(nbMove)
				.append(" moves.").toString());
		HanoiContext.getInstance().fireComputationCompleted();
	}

	private final static String DISCS_PREFIX  = "--discs:";
	private final static String CLAW_PREFIX   = "--claw:";
	private final static String LEFT_PREFIX   = "--left:";
	private final static String RIGHT_PREFIX  = "--right:";
	private final static String BOTTOM_PREFIX = "--bottom:";

	public static void main(String... args) {

		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith(DISCS_PREFIX)) {
					try {
						nbDisc = Integer.parseInt(prm.substring(DISCS_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(CLAW_PREFIX)) {
					try {
						claw = Integer.parseInt(prm.substring(CLAW_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(LEFT_PREFIX)) {
					try {
						left = Integer.parseInt(prm.substring(LEFT_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(RIGHT_PREFIX)) {
					try {
						right = Integer.parseInt(prm.substring(RIGHT_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (prm.startsWith(BOTTOM_PREFIX)) {
					try {
						bottom = Integer.parseInt(prm.substring(BOTTOM_PREFIX.length()));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.out.println("Duh?");
				}
			}
		}

		System.out.println(String.format("With %d discs, from A to C", nbDisc));

		hanoiStand = new HanoiContext.Stand("A", "B", "C");
		String initialPost = "A";
		hanoiStand.initStand(nbDisc, initialPost);

		try {
			MeArmPilot.initContext(left, claw, bottom, right);
		} catch (I2CFactory.UnsupportedBusNumberException ex) {
			System.out.println("Ooops, no I2C bus...");
		} catch (Exception ioe) {
			System.out.println("Is the PCA9685 connected?");
		}

		HanoiContext.getInstance().fireSetNbDisc(nbDisc);

		int nbCommand = 0;

		// TODO Init and calibrate here

		String cmd = "PRINT: Ready";

		try {
			++nbCommand;
			MeArmPilot.validateCommand(cmd, nbCommand);
			MeArmPilot.executeCommand(cmd, nbCommand);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

			public void computationCompleted() {
				System.out.println("Completed!");
				synchronized (me) {
					me.notify();
				}
			}
		});

		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

			public void moveRequired(String from, String to) {
				nbMove++;

				HanoiContext.Post fromPost = hanoiStand.getPost(from);
				HanoiContext.Post toPost = hanoiStand.getPost(to);
				Integer discToMove = fromPost.getTopDisc();
				Integer otherDisc = toPost.getTopDisc();
				if (otherDisc != null && otherDisc.intValue() != 0 && otherDisc.intValue() < discToMove.intValue()) {
					JOptionPane.showMessageDialog(null, (new StringBuilder()).append("Un-authorized move!!!\n").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString(), "Error", 0);
					if ("true".equals(System.getProperty("fail.on.forbidden.move", "false"))) {
						throw new RuntimeException((new StringBuilder()).append("Un-authorized move, ").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString());
					}
				}

				System.out.println((new StringBuilder())
						.append("Moving from ")
						.append(String.format("%s pos %d", from, fromPost.getDiscCount()))
						.append(" to ")
						.append(String.format("%s, currently %d disc(s)", to, toPost.getDiscCount())).toString());


				// TODO The actual move...
				// Simulate wait
				try { Thread.sleep(1000); } catch (Exception ex) {}

				fromPost.removeTopDisc();
				toPost.add(discToMove);
			}

			public void startComputation() {
				System.out.println("Starting resolution");
				Thread solver = new Thread(() -> startSolving());
				solver.start();
			}
		});
		// Process
		HanoiContext.getInstance().fireStartComputation();

		me = Thread.currentThread();
		synchronized (me) {
			try {
				me.wait();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		/*
#
# Stop and Park the servos
#
SET_PWM:LEFT,   0, 0
SET_PWM:RIGHT,  0, 0
SET_PWM:CLAW,   0, 0
SET_PWM:BOTTOM, 0, 0
		 */

		System.out.println("Done.");
	}
}
