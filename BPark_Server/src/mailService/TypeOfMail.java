package mailService;

/**
 * Defines the different types of emails that can be sent by the system.
 */
public enum TypeOfMail {
    
    /** Email for sending a lost or forgotten password. */
    FORGOT_PASSWORD,
    
    /** Email notification for a late vehicle retrieval. */
    LATE,
    
    /** A generic informational message. */
    GENERIC_MESSAGE
}
