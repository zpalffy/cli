package com.eric;

import java.util.ArrayList;
import java.util.Collection;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * This class provides some common things I do when creating a command-line
 * utility via com.beust.jcommander.JCommander. To use it, write a class that
 * extends this class and provide a main method like:
 * 
 * <pre>
 * public static void main(String[] args) {
 * 	Command.main(new MyCommandExtension(), args);
 * }
 * </pre>
 * 
 * Parameter's in this class are specified just as they normally would, via
 * com.beust.jcommander.Parameter annotations.
 */
public abstract class Command {

	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;

	@Parameter(names = { "-v", "--verbose" }, description = "Displays more output.")
	protected boolean verbose;

	@Parameter(names = "--debug", hidden = true)
	private boolean debug;

	/**
	 * Override to provide messages that are displayed if certain prerequisites
	 * are not met. If a parameter or combination of parameters is not valid,
	 * just add a message the the messages collection and it will be displayed
	 * appropriately.
	 */
	protected void validate(Collection<String> messages) {
	}

	/**
	 * @return the name of the program as displayed in a help message. If using
	 *         gradle with the application plugin, this should match the
	 *         'applicationName' property in the build file.
	 */
	protected abstract String getProgramName();

	/**
	 * Actually run the program. This could have come from java.lang.Runnable
	 * but I wanted to be able to throw checked exceptions to stop execution of
	 * the program
	 */
	protected abstract void run() throws Exception;

	/**
	 * Write to System.out
	 */
	protected void out(String format, Object... args) {
		System.out.println(String.format(format, args));
	}

	protected void verbose(String format, Object... args) {
		if (verbose) {
			out(format, args);
		}
	}

	protected void debug(String format, Object... args) {
		if (debug) {
			out(format, args);
		}
	}

	/**
	 * Write the message to System.err. If 'debug' flag is on, print the stack
	 * trace to System.err as well.
	 */
	protected void err(String message, Exception ex) {
		err("There was a problem, trying running with the --debug option for more details.  %s",
				message);

		if (debug) {
			ex.printStackTrace();
		}
	}

	/**
	 * Write the message to System.err.
	 */
	protected void err(String format, Object... args) {
		if (format != null) {
			System.err.println(String.format(format, args));
		}
	}

	protected void exit(int code) {
		debug("Exiting with code %s", code);
		System.exit(code);
	}

	public String getWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	public static void main(Command cmd, String... args) {
		JCommander jc = new JCommander(cmd);

		try {
			jc.setProgramName(cmd.getProgramName());
			jc.parse(args);

			if (cmd.help) {
				jc.usage();
			} else {
				Collection<String> messages = new ArrayList<String>();
				cmd.validate(messages);
				if (messages.isEmpty()) {
					cmd.run();
				} else {
					for (String m : messages) {
						cmd.err(m);
					}

					jc.usage();
					cmd.exit(1);
				}
			}
		} catch (NullPointerException npe) {
			cmd.err("There was a problem, trying running with the --debug option for more details.",
					npe);
			cmd.exit(3);
		} catch (Exception e) {
			cmd.err(e.getMessage(), e);
			cmd.exit(2);
		}
	}
}