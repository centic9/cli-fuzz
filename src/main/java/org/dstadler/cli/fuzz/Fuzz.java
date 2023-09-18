package org.dstadler.cli.fuzz;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

/**
 * This class provides a simple target for fuzzing Apache Commons CLI with Jazzer.
 *
 * It uses the fuzzed input data to call various method which parse some commandline
 * options.
 *
 * It catches all exceptions that are currently expected.
 */
public class Fuzz {
	public static void fuzzerTestOneInput(FuzzedDataProvider data) {
		// try to invoke various methods which parse commandline input
		List<String> args = new ArrayList<>();
		for (int i = 0;i < 100;i++) {
			String arg = data.consumeString(100);
			if (arg != null && arg.length() > 0) {
				args.add(arg);
			}
		}
		String remaining = data.consumeRemainingAsString();
		if (remaining != null) {
			args.add(remaining);
		}

		String[] argsArray = args.toArray(new String[0]);

		//noinspection deprecation
		for (CommandLineParser parser : new CommandLineParser[] {
				new DefaultParser(),
				new PosixParser(),
				new GnuParser(),
				new BasicParser()
		}) {
			check_ls(argsArray, parser);
			check_ant(argsArray, parser);
		}
		check_dynamic_options(argsArray);
	}

	private static void check_ls(String[] args, CommandLineParser parser) {
		// create the command line parser

		// create the Options
		Options options = new Options();
		options.addOption("a", "all", false, "do not hide entries starting with .");
		options.addOption("A", "almost-all", false, "do not list implied . and ..");
		options.addOption("b", "escape", false, "print octal escapes for non-graphic "
				+ "characters");
		options.addOption(Option.builder("SIZE").longOpt("block-size")
				.desc("use SIZE-byte blocks")
				.hasArg()
				.build());
		options.addOption("B", "ignore-backups", false, "do not list implied entries "
				+ "ending with ~");
		options.addOption("c", false, "with -lt: sort by, and show, ctime (time of last "
				+ "modification of file status information) with "
				+ "-l:show ctime and sort by name otherwise: sort "
				+ "by ctime");
		options.addOption("C", false, "list entries by columns");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			line.hasOption('a');
			line.hasOption('d');
			line.hasOption("ignore-backups");
			line.hasOption("abc");
			line.hasOption(options.getOptions().iterator().next());

			Iterator<Option> it = line.iterator();
			while (it.hasNext()) {
				Option op = it.next();
				//noinspection ResultOfMethodCallIgnored
				op.getLongOpt();
				op.getValues();
				//noinspection ResultOfMethodCallIgnored
				op.getArgs();
				op.getValue();
			}

			line.getOptions();
			line.getArgs();
			//noinspection ResultOfMethodCallIgnored
			line.getArgList();
		} catch (ParseException exp) {
			// expected here
		} catch (StringIndexOutOfBoundsException e) {
			// Reported at https://issues.apache.org/jira/browse/CLI-313
			// can be removed here when the bug is resolved
		}
	}

	private static void check_ant(String[] args, CommandLineParser parser) {
		Option help = new Option("help", "print this message");
		Option projecthelp = new Option("projecthelp", "print project help information");
		Option version = new Option("version", "print the version information and exit");
		Option quiet = new Option("quiet", "be extra quiet");
		Option verbose = new Option("verbose", "be extra verbose");
		Option debug = new Option("debug", "print debugging information");
		Option emacs = new Option("emacs",
				"produce logging information without adornments");

		Option logfile   = Option.builder("logfile")
				.argName("file")
				.hasArg()
				.desc("use given file for log")
				.build();

		Option logger    = Option.builder("logger")
				.argName("classname")
				.hasArg()
				.desc("the class which it to perform logging")
				.build();

		Option listener  = Option.builder("listener")
				.argName("classname")
				.hasArg()
				.desc("add an instance of class as "
						+ "a project listener")
				.build();

		Option buildfile = Option.builder("buildfile")
				.argName("file")
				.hasArg()
				.desc("use given buildfile")
				.build();

		Option find      = Option.builder("find")
				.argName("file")
				.hasArg()
				.desc("search for buildfile towards the "
						+ "root of the filesystem and use it")
				.build();

		Option property  = Option.builder("D")
				.hasArgs()
				.valueSeparator('=')
				.build();

		Options options = new Options();

		options.addOption(help);
		options.addOption(projecthelp);
		options.addOption(version);
		options.addOption(quiet);
		options.addOption(verbose);
		options.addOption(debug);
		options.addOption(emacs);
		options.addOption(logfile);
		options.addOption(logger);
		options.addOption(listener);
		options.addOption(buildfile);
		options.addOption(find);
		options.addOption(property);

		// create the parser
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			line.hasOption('a');
			line.hasOption('D');
			line.hasOption("logfile");
			line.hasOption("D");
			line.hasOption("abc");
			line.hasOption(options.getOptions().iterator().next());
		} catch (ParseException exp) {
			// expected here
		} catch (StringIndexOutOfBoundsException e) {
			// Reported at https://issues.apache.org/jira/browse/CLI-313
			// can be removed here when the bug is resolved
		}
	}

	private static void check_dynamic_options(String[] args) {
		Options options = new Options();

		for (String arg : args) {
			try {
				options.addOption(arg, "");
			} catch (IllegalArgumentException e) {
				// expected on invalid option definitions
			}
		}

		PrintStream out_save = System.out;
		try {
			System.setOut(new PrintStream(new ByteArrayOutputStream()));
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ant", options);
		} finally {
			System.setOut(out_save);
		}
	}
}
