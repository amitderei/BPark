package common;

import java.io.Serializable;

/**
 * This class represents a subscriber in the BPARK system.
 * Each subscriber has a unique code (assigned by the database),
 * identification details (like ID, name, phone number, email),
 * login credentials (username), and optionally an RFID tag
 * for automatic identification by vehicle.
 * Objects of this class are sent between client and server
 * to share subscriber-related information.
 */
@SuppressWarnings("serial")
public class Subscriber implements Serializable {

    /** Unique code for the subscriber (assigned by the database) */
    private int subscriberCode;

    /** National ID (Israeli ID, 9-digit format) */
    private String userId;

    /** Subscriber's first name */
    private String firstName;

    /** Subscriber's last name */
    private String lastName;

    /** Mobile phone number (must begin with 05...) */
    private String phoneNum;

    /** Email address used for communication */
    private String email;

    /** Username used to log in to the system */
    private String username;

    /** Optional RFID tag used to identify the subscriber's vehicle */
    private String tagId;

    /**
     * Full constructor — used when loading a complete subscriber record from the database.
     *
     * @param subscriberCode unique internal code
     * @param userId national ID
     * @param firstName subscriber's first name
     * @param lastName subscriber's last name
     * @param phoneNum mobile phone number
     * @param email contact email
     * @param username system login name
     * @param tagId optional RFID tag
     */
    public Subscriber(int subscriberCode,
                      String userId,
                      String firstName,
                      String lastName,
                      String phoneNum,
                      String email,
                      String username,
                      String tagId) {
        this.subscriberCode = subscriberCode;
        this.userId         = userId;
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.phoneNum       = phoneNum;
        this.email          = email;
        this.username       = username;
        this.tagId          = tagId;
    }

    /**
     * Constructor used when we only know the subscriber code (e.g., for lookup).
     *
     * @param subscriberCode unique ID of the subscriber
     */
    public Subscriber(int subscriberCode) {
        this.subscriberCode = subscriberCode;
    }

    /**
     * Default constructor - required for frameworks and serialization.
     */
    public Subscriber() { }

    /**
     * Constructor used when only the username is needed (e.g., for login check).
     *
     * @param username login name of the subscriber
     */
    public Subscriber(String username) {
        this.username = username;
    }

    /**
     * @return the unique subscriber code
     */
    public int getSubscriberCode() {
        return subscriberCode;
    }

    /**
     * @return the national ID number
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return the first name of the subscriber
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the last name of the subscriber
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the mobile phone number
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * @return the subscriber's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the username used to log in
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the RFID tag ID, or null if not set
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * Sets the unique subscriber code.
     * @param code the internal code from the database
     */
    public void setSubscriberCode(int code) {
        this.subscriberCode = code;
    }

    /**
     * Sets the subscriber's national ID.
     * @param userId the 9-digit ID number
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the subscriber's first name.
     * @param firstName the given name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the subscriber's last name.
     * @param lastName the family name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the phone number.
     * @param phoneNum number in the format 05XXXXXXXX
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * Sets the email address.
     * @param email valid email string
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the login username.
     * @param username login name used by the subscriber
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the RFID tag identifier.
     * @param tagId the tag ID attached to the subscriber's vehicle
     */
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    /**
     * Compares all main subscriber fields (excluding tagId) to check if two subscriber
     * records represent the same data.
     *
     * @param a first subscriber
     * @param b second subscriber
     * @return true if all fields match, false otherwise
     */
    public static boolean equals(Subscriber a, Subscriber b) {
        return  a.subscriberCode == b.subscriberCode &&
                a.userId        .equals(b.userId)    &&
                a.firstName     .equals(b.firstName) &&
                a.lastName      .equals(b.lastName)  &&
                a.phoneNum      .equals(b.phoneNum)  &&
                a.email         .equals(b.email)     &&
                a.username      .equals(b.username);
    }
}
