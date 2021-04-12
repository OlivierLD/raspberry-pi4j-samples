package oliv.events;

import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class client {

    private final static String HELP_PREFIX =           "--help";
    private final static String CLIENT_NAME_PREFIX =    "--client-name:";
    private final static String CLIENT_VERBOSE_PREFIX = "--client-verbose:";
    private final static String CLIENT_SPEECH_PREFIX =  "--client-speech:";
    private final static String SERVER_NAME_PREFIX =    "--server-name:";
    private final static String SERVER_PORT_PREFIX =    "--server-port:";

    private final static String HELP_SMALL_PREFIX =           "-h";
    private final static String CLIENT_NAME_SMALL_PREFIX =    "-c:";
    private final static String CLIENT_VERBOSE_SMALL_PREFIX = "-v:";
    private final static String CLIENT_SPEECH_SMALL_PREFIX =  "-s:";
    private final static String SERVER_NAME_SMALL_PREFIX =    "-n:";
    private final static String SERVER_PORT_SMALL_PREFIX =    "-p:";

    public static class TextToSpeech {
        private static final Map<String, Consumer<String>> speechTools = new HashMap<>();

        static Consumer<String> say = message -> {
            try {
                // User say -v ? for a list of voices. TODO: a runtime option for the language?
//			    Runtime.getRuntime().exec(new String[] { "say", "-v", "Thomas", "\"" + message + "\"" }); // French
                Runtime.getRuntime().exec(new String[] { "say", "-v", "Alex", "\"" + message + "\"" });   // English
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        static Consumer<String> espeak = message -> {
            // On Raspberry Pi, do a "sudo apt-get install espeak"
            try {
                Runtime.getRuntime().exec(new String[] { "espeak", "\"" + message + "\"" });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        static {
            speechTools.put("Mac OS X", say);
            speechTools.put("Linux", espeak);
//            speechTools.put("Windows", espeak); // Or https://github.com/p-groarke/wsay/releases
        }

        public static void speak(String text) {
            Consumer<String> speechTool = speechTools.get(System.getProperty("os.name"));
            if (speechTool == null) {
                throw new RuntimeException("No speech tool found in this os [" + System.getProperty("os.name") + "]");
            }
            try {
                speechTool.accept(text);
                if (originalMessageConsumer != null) { // double up!
                    originalMessageConsumer.accept(text);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static Consumer<String> originalMessageConsumer = null;

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static void displayHelp() {
        System.out.println("---- TCP Chat Client ----");
        System.out.println("CLI Parameters:");
        System.out.printf("+-%s-+-%s-+--------------------------------------------%n",
                Utils.rpad("", 16, "-"),
                Utils.rpad("", 32, "-"));
        System.out.printf("| %s | %s | - Display help and exit.%n",
                Utils.rpad(String.format("%s", HELP_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%s", HELP_PREFIX), 32));
        System.out.printf("| %s | %s | - Verbose mode, default false.%n",
                Utils.rpad(String.format("%strue|false", CLIENT_VERBOSE_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%strue|false", CLIENT_VERBOSE_PREFIX), 32));
        System.out.printf("| %s | %s | - Server name or IP address. Default is localhost.%n",
                Utils.rpad(String.format("%slocalhost", SERVER_NAME_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%slocalhost", SERVER_NAME_PREFIX), 32));
        System.out.printf("| %s | %s | - Server TCP Port, default 7001.%n",
                Utils.rpad(String.format("%s7001", SERVER_PORT_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%s7001", SERVER_PORT_PREFIX), 32));
        System.out.printf("| %s | %s | - Client name, defaulted to hostname (%s here).%n",
                Utils.rpad(String.format("%sraspi", CLIENT_NAME_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%sraspi", CLIENT_NAME_PREFIX), 32),
                getHostName());
        System.out.printf("| %s | %s | - Client speaks on message received (experimental), default false.%n",
                Utils.rpad(String.format("%strue|false", CLIENT_SPEECH_SMALL_PREFIX), 16),
                Utils.rpad(String.format("%strue|false", CLIENT_SPEECH_PREFIX), 32));
        System.out.printf("+-%s-+-%s-+--------------------------------------------%n",
                Utils.rpad("", 16, "-"),
                Utils.rpad("", 32, "-"));
    }

    public static void main(String... args) {

        // Default values
        String clientName = null; // "It's Me!";
        String chatServerName = "localhost";
        int chatServerPort = 7001;
        boolean verbose = false;
        boolean speech = false;

        for (String arg : args) {
            if (arg.startsWith(CLIENT_NAME_PREFIX)) {
                clientName = arg.substring(CLIENT_NAME_PREFIX.length());
            } else if (arg.startsWith(CLIENT_NAME_SMALL_PREFIX)) {
                clientName = arg.substring(CLIENT_NAME_SMALL_PREFIX.length());
            } else if (arg.startsWith(SERVER_NAME_PREFIX)) {
                chatServerName = arg.substring(SERVER_NAME_PREFIX.length());
            } else if (arg.startsWith(SERVER_NAME_SMALL_PREFIX)) {
                chatServerName = arg.substring(SERVER_NAME_SMALL_PREFIX.length());
            } else if (arg.startsWith(CLIENT_VERBOSE_PREFIX)) {
                verbose = "true".equals(arg.substring(CLIENT_VERBOSE_PREFIX.length()));
            } else if (arg.startsWith(CLIENT_VERBOSE_SMALL_PREFIX)) {
                verbose = "true".equals(arg.substring(CLIENT_VERBOSE_SMALL_PREFIX.length()));
            } else if (arg.startsWith(CLIENT_SPEECH_PREFIX)) {
                speech = "true".equals(arg.substring(CLIENT_SPEECH_PREFIX.length()));
            } else if (arg.startsWith(CLIENT_SPEECH_SMALL_PREFIX)) {
                speech = "true".equals(arg.substring(CLIENT_SPEECH_SMALL_PREFIX.length()));
            } else if (arg.startsWith(SERVER_PORT_PREFIX)) {
                chatServerPort = Integer.parseInt(arg.substring(SERVER_PORT_PREFIX.length()));
            } else if (arg.startsWith(SERVER_PORT_SMALL_PREFIX)) {
                chatServerPort = Integer.parseInt(arg.substring(SERVER_PORT_SMALL_PREFIX.length()));
            } else if (arg.equals(HELP_PREFIX) || arg.equals(HELP_SMALL_PREFIX)) {
                displayHelp();
                System.exit(0);
            }
        }

        ChatTCPClient client = new ChatTCPClient(chatServerName, chatServerPort, verbose);

        // Optional: overrides the default action, make it speak...
        if (speech) {
            originalMessageConsumer = client.getMessageConsumer();
            client.setMessageConsumer(TextToSpeech::speak);
        }

        final Thread me = Thread.currentThread();
        Thread listener = new Thread(() -> {
            client.startClient(me); // Initialize the socket, and waits in a loop.
        });
        listener.start();

        // Wait for the socket stuff to start
        try {
//          Thread.sleep(1_000L); // Bad approach: See below something nicer
            synchronized (me) {
                me.wait();
                System.out.println("Done with client initialization.");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        if (clientName == null) {
            clientName = Objects.requireNonNullElse(getHostName(), "DefaultedClientName");
        }
        String idMess = String.format("%s:%s", ChatTCPServer.SERVER_COMMANDS.I_AM.toString(), clientName);
        System.out.printf(">>> Telling server who I am: %s\n", idMess);
        client.writeToServer(idMess);

        // Client input part

        System.out.println("Q or QUIT to quit");
        System.out.println("WHO_S_THERE to know who's there");
        System.out.println("Anything else will be broadcasted");

        if (speech) {
            TextToSpeech.speak("Client is ready!");
        }

        Console console = System.console();
        boolean keepAsking = true;
        while (keepAsking) {
            if (console != null) {
                System.out.print("> ");
                String userInput = console.readLine();
                if (!userInput.isEmpty()) {
                    System.out.printf("Processing user input [%s]\n", userInput);
                    if ("Q".equalsIgnoreCase(userInput) || "QUIT".equalsIgnoreCase(userInput)) {
                        keepAsking = false;
                        client.writeToServer(ChatTCPServer.SERVER_COMMANDS.I_M_OUT.toString());
                    } else {
                        client.writeToServer(userInput);
                    }
                }
            } else {
                System.out.println("No System.console...?");
                try { Thread.sleep(1_000L); }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        System.out.println("Cleaning up...");
        try {
            client.closeClient();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
