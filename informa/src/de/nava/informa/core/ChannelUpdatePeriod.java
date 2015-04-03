package de.nava.informa.core;

public enum ChannelUpdatePeriod {
  /** Update of channel expected to be specified in number of hours */
  UPDATE_HOURLY("hourly", 1000 * 60 * 60),

  /** Update of channel expected to be specified in number of days */
  UPDATE_DAILY("daily", 1000 * 60 * 60 * 24),

  /** Update of channel expected to be specified in number of weeks */
  UPDATE_WEEKLY("weekly", 1000 * 60 * 60 * 24 * 7),

  /** Update of channel expected to be specified in number of months */
  UPDATE_MONTHLY("monthly", 1000 * 60 * 60 * 24 * 30),

  /** Update of channel expected to be specified in number of years */
  UPDATE_YEARLY("yearly", 1000 * 60 * 60 * 24 * 365);

  /**
   * Text representation of the period.
   */
  private String text;

  /**
   * Miliseconds in the period.
   */
  private long msInPeriod;

  /**
   * @param text Text representation of the period.
   * @param msInPeriod Miliseconds in the period.
   */
  private ChannelUpdatePeriod(String text, long msInPeriod) {
    this.text = text;
    this.msInPeriod = msInPeriod;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.text;
  }

  /**
   * @return Miliseconds in the period.
   */
  public long getMsInPeriod() {
    return this.msInPeriod;
  }

  /**
   * @return Minutes in the period.
   */
  public int getMinutesInPeriod() {
    return (int) (this.msInPeriod / 1000 / 60);
  }

  /**
   * @param text Text representation of the period.
   * @return The channel update period specified by the text representation.
   */
  public static ChannelUpdatePeriod valueFromText(String text) {
    for (ChannelUpdatePeriod updatePeriod : values()) {
      if (updatePeriod.text.equals(text)) {
        return updatePeriod;
      }
    }

    throw new IllegalArgumentException("The text representation '" + text
        + "' is not valid for ChannelUpdatePeriod.");
  }
}
