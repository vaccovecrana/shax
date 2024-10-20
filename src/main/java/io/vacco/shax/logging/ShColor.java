package io.vacco.shax.logging;

import static java.lang.String.format;

public class ShColor {

  private static final String SPC = " ";
  private static final String FMT = "%s%s%s";
  private static final String RESET = "\u001b[0m";

  private static final String BLUE_PALE = "\u001b[38;5;66m";
  private static final String BLACK_BOLD_BRIGHT = "\u001b[1;90m";
  private static final String RED_BOLD_BRIGHT = "\u001b[1;91m";
  private static final String GREEN_BOLD_BRIGHT   = "\u001b[1;92m";
  private static final String YELLOW_BOLD_BRIGHT  = "\u001b[1;93m";
  private static final String BLUE_BOLD_BRIGHT    = "\u001b[1;94m";
  private static final String MAGENTA_BOLD_BRIGHT = "\u001b[1;95m";
  private static final String CYAN_BOLD_BRIGHT    = "\u001b[1;96m";

  private static String fmt(String color, String txt) {
    return format(FMT, color, txt, RESET);
  }

  public static String bluePale(String txt) { return fmt(BLUE_PALE, txt); }
  public static String blackBoldBright(String txt) { return fmt(BLACK_BOLD_BRIGHT, txt); }
  public static String redBoldBright(String txt) { return fmt(RED_BOLD_BRIGHT, txt); }
  public static String greenBoldBright(String txt) { return fmt(GREEN_BOLD_BRIGHT, txt); }
  public static String yellowBoldBright(String txt) { return fmt(YELLOW_BOLD_BRIGHT, txt); }
  public static String blueBoldBright(String txt) { return fmt(BLUE_BOLD_BRIGHT, txt); }
  public static String magentaBoldBright(String txt) { return fmt(MAGENTA_BOLD_BRIGHT, txt); }
  public static String cyanBoldBright(String txt) { return fmt(CYAN_BOLD_BRIGHT, txt); }

  public static String labelFor(ShLogLevel l) {
    switch (l) {
      case DEBUG: return blueBoldBright(l.name());
      case INFO:  return greenBoldBright(l.name() + SPC);
      case WARN:  return yellowBoldBright(l.name() + SPC); // aligns log level labels nicely in dev mode.
      case ERROR: return redBoldBright(l.name());
      case TRACE: return cyanBoldBright(l.name());
    }
    return l.name();
  }

}
