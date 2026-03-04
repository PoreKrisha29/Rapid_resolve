import java.util.Scanner;

/**
 * Console UI helper utilities:
 * - Styling (colors, ASCII art heading)
 * - Boxed menus and information panels
 * - Robust input prompts and validation
 * - Simple effects (typewriter, loading dots)
 */
public class CLIUtils {
    // ANSI escape codes for colors
    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String WHITE = "\u001B[37m";

    // Print the block ASCII art heading ONCE
    /**
     * Print the application heading (ASCII art). If the fancy print fails,
     * a simple text heading is used as a fallback.
     */
    public static void printSingleBlockHeading() {
        try {
            String[] art = {
                    CYAN + "██████╗  █████╗ ██████╗ ██╗██████╗     ██████╗ ███████╗███████╗ ██████╗ ██╗    ██╗   ██╗███████╗    " + RESET,
                    CYAN + "██╔══██╗██╔══██╗██╔══██╗██║██╔══██╗    ██╔══██╗██╔════╝██╔════╝██╔═══██╗██║    ██║   ██║██╔════╝    " + RESET,
                    CYAN + "██████╔╝███████║██████╔╝██║██║  ██║    ██████╔╝█████╗  ███████╗██║   ██║██║    ██║   ██║█████╗      " + RESET,
                    CYAN + "██╔══██╗██╔══██║██╔═══╝ ██║██║  ██║    ██╔══██╗██╔══╝  ╚════██║██║   ██║██║    ╚██╗ ██╔╝██╔══╝      " + RESET,
                    CYAN + "██║  ██║██║  ██║██║     ██║██████╔╝    ██║  ██║███████╗███████║╚██████╔╝███████╗╚████╔╝ ███████╗    " + RESET,
                    CYAN + "╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═════╝     ╚═╝  ╚═╝╚══════╝╚══════╝ ╚═════╝ ╚══════╝ ╚═══╝  ╚══════╝    " + RESET,
                    CYAN + "                                                                                                    " + RESET,
                    CYAN + "                    " + BLUE + " COMPLAINT HANDLING PORTAL " + RESET + CYAN + "                    " + RESET
            };
            for (String line : art) {
                if (line != null) {
                    System.out.println(center(line, 110));
                }
            }
            System.out.println();
        } catch (Exception e) {
            // Fallback to simple heading if ASCII art fails
            try {
                System.out.println("RAPIDRESOLVE - Complaint & Crime Management System");
                System.out.println();
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Center a string in a given width
    /**
     * Center a string to the specified width using spaces.
     * @param s text to center
     * @param width total width to center into
     * @return centered string (or original if longer than width)
     */
    public static String center(String s, int width) {
        try {
            if (s == null) {
                s = "";
            }
            if (s.length() >= width) return s;
            int left = (width - s.length()) / 2;
            int right = width - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        } catch (Exception e) {
            return s != null ? s : "";
        }
    }

    // Clear the screen (works on most ANSI terminals)
    /**
     * Attempt to clear the console using ANSI sequences. Falls back to printing
     * blank lines if the terminal does not support ANSI.
     */
    public static void clearScreen() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            // If clear screen fails, just print some newlines
            try {
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
            } catch (Exception ex) {
                // If even newlines fail, do nothing
            }
        }
    }


    // Print a random tip of the day
    /**
     * Print a random tip-of-the-day line to the console.
     */
    public static void printTipOfTheDay() {
        try {
            String[] tips = {
                    "Tip: Use strong passwords for your account!",
                    "Tip: You can check complaint status anytime from your dashboard.",
                    "Tip: Officers are assigned based on area workload.",
                    "Tip: Use the feedback option to help us improve!",
                    "Tip: You can export your complaint history as a report.",
                    "Tip: For emergencies, use the helpline numbers shown in the menu.",
                    "Tip: Keep your profile updated for better service.",
                    "Tip: Use the FAQs section for quick help.",
                    "Tip: Admins can generate system-wide reports from their dashboard.",
                    "Tip: All actions are logged for transparency and security."
            };
            int idx = (int)(Math.random() * tips.length);
            System.out.println(MAGENTA + "\n💡 " + tips[idx] + RESET);
        } catch (Exception e) {
            try {
                System.out.println("\n💡 Tip: Keep your account secure!");
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Improved boxed menu with double-line borders and arrow options, with tip of the day
    /**
     * Print a styled boxed menu with a title and options. Also prints a random tip below.
     * @param title menu title
     * @param options options list (lines)
     */
    public static void printBoxedMenu(String title, String[] options) {
        // Calculate the max width needed
        int maxLen = title.length();
        for (String opt : options) {
            // Account for arrow and space ("→ ")
            int optLen = 2 + 1 + opt.length();
            if (optLen > maxLen) maxLen = optLen;
        }
        int padding = 2; // spaces on each side
        int width = maxLen + padding * 2;

        // Top border
        String borderTop = CYAN + "╔" + "═".repeat(width) + "╗" + RESET;
        // Title separator
        String sep = CYAN + "╠" + "═".repeat(width) + "╣" + RESET;
        // Bottom border
        String borderBottom = CYAN + "╚" + "═".repeat(width) + "╝" + RESET;

        // Title line, centered
        int titlePadLeft = (width - title.length()) / 2;
        int titlePadRight = width - title.length() - titlePadLeft;
        String titleLine = CYAN + "║" + " ".repeat(titlePadLeft) + YELLOW + title + RESET + " ".repeat(titlePadRight) + CYAN + "║" + RESET;

        // Print box
        System.out.println(borderTop);
        System.out.println(titleLine);
        System.out.println(sep);
        for (String opt : options) {
            String line = " ".repeat(padding) + BLUE + "→ " + opt + RESET;
            int optPadRight = width - (line.replaceAll("\\u001B\\[[;\\d]*m", "").length());
            System.out.println(CYAN + "║" + line + " ".repeat(optPadRight) + CYAN + "║" + RESET);
        }
        System.out.println(borderBottom);
        printTipOfTheDay();
    }

    // Print a boxed info section (for About, Help, etc.)
    /**
     * Print a styled boxed text section with a title and lines of content.
     * @param title box title
     * @param lines content lines
     */
    public static void printBoxedInfo(String title, String[] lines) {
        if (title == null) title = "Information";
        if (lines == null || lines.length == 0) {
            lines = new String[]{"No information available"};
        }

        int maxLen = title.length();
        for (String l : lines) {
            if (l != null && l.length() > maxLen) maxLen = l.length();
        }
        int padding = 2;
        int width = maxLen + padding * 2;

        String borderTop = CYAN + "╔" + "═".repeat(width) + "╗" + RESET;
        String sep = CYAN + "╠" + "═".repeat(width) + "╣" + RESET;
        String borderBottom = CYAN + "╚" + "═".repeat(width) + "╝" + RESET;

        int titlePadLeft = (width - title.length()) / 2;
        int titlePadRight = width - title.length() - titlePadLeft;
        String titleLine = CYAN + "║" + " ".repeat(titlePadLeft) + YELLOW + title + RESET + " ".repeat(titlePadRight) + CYAN + "║" + RESET;

        System.out.println(borderTop);
        System.out.println(titleLine);
        System.out.println(sep);
        for (String l : lines) {
            String line = " ".repeat(padding) + WHITE + l + RESET;
            int linePadRight = width - (line.replaceAll("\\u001B\\[[;\\d]*m", "").length());
            System.out.println(CYAN + "║" + line + " ".repeat(linePadRight) + CYAN + "║" + RESET);
        }
        System.out.println(borderBottom);
    }

    // Pause and wait for user to press Enter
    /**
     * Block until the user presses Enter.
     */
    public static void waitForEnter() {
        try {
            System.out.print(MAGENTA + "\nPress Enter to continue..." + RESET);
            System.in.read();
        } catch (Exception e) {
            try {
                System.out.println("\nPress Enter to continue...");
                System.in.read();
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Print a colored prompt
    /**
     * Print a colored prompt label without a trailing newline.
     * @param prompt text to display before input
     */
    public static void printPrompt(String prompt) {
        try {
            if (prompt == null) prompt = "Enter: ";
            System.out.print(YELLOW + prompt + RESET);
        } catch (Exception e) {
            try {
                System.out.print(prompt != null ? prompt : "Enter: ");
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Print a colored error
    /**
     * Print an error line in red color.
     * @param error message to display
     */
    public static void printError(String error) {
        try {
            if (error == null) error = "An error occurred";
            System.out.println(RED + error + RESET);
        } catch (Exception e) {
            try {
                System.out.println("ERROR: " + (error != null ? error : "An error occurred"));
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Print a colored info message
    /**
     * Print an informational line in green color.
     * @param info message to display
     */
    public static void printInfo(String info) {
        try {
            if (info == null) info = "";
            System.out.println(GREEN + info + RESET);
        } catch (Exception e) {
            try {
                System.out.println(info != null ? info : "");
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }



    // Print a colored success message
    /**
     * Print a success line.
     * @param msg message to display
     */
    public static void printSuccess(String msg) {
        try {
            if (msg == null) msg = "Success";
            System.out.println(GREEN + msg + RESET);
        } catch (Exception e) {
            try {
                System.out.println("SUCCESS: " + (msg != null ? msg : "Success"));
            } catch (Exception ex) {
                // If even fallback fails, do nothing
            }
        }
    }

    // Robust input utility: get validated integer input with re-prompt
    /**
     * Read an integer within bounds with re-prompt on invalid input.
     * @param sc shared scanner
     * @param prompt label
     * @param min inclusive lower bound
     * @param max inclusive upper bound
     * @return validated integer value
     */
    public static int promptInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            try {
                printPrompt(prompt);
                String input = sc.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    printError("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                printError("Please enter a valid number.");
            } catch (Exception e) {
                printError("Input error: " + e.getMessage());
            }
        }
    }

    // Robust input utility: get validated string input with re-prompt
    /**
     * Read a string with constraints (required/length/regex) and re-prompt on invalid input.
     * @param sc shared scanner
     * @param prompt label
     * @param required whether empty input is not allowed
     * @param minLen minimum length (0 to disable)
     * @param maxLen maximum length (0 to disable)
     * @param regex optional regex for validation (null to disable)
     * @param errorMsg message on regex violation (or a generic one)
     * @return validated string value
     */
    public static String promptString(Scanner sc, String prompt, boolean required, int minLen, int maxLen, String regex, String errorMsg) {
        while (true) {
            try {
                printPrompt(prompt);
                String input = sc.nextLine().trim();
                if (required && input.isEmpty()) {
                    printError(prompt + " cannot be empty. Please try again.");
                    continue;
                }
                if (minLen > 0 && input.length() < minLen) {
                    printError(prompt + " must be at least " + minLen + " characters long.");
                    continue;
                }
                if (maxLen > 0 && input.length() > maxLen) {
                    printError(prompt + " must be at most " + maxLen + " characters long.");
                    continue;
                }
                if (regex != null && !regex.isEmpty() && !input.matches(regex)) {
                    printError(errorMsg != null ? errorMsg : (prompt + " is invalid."));
                    continue;
                }
                return input;
            } catch (Exception e) {
                printError("Input error: " + e.getMessage());
            }
        }
    }
}