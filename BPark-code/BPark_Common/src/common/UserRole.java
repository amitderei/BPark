package common;

import java.io.Serializable;

/**
 * Enumeration of all user roles in the BPARK system.
 * This enum defines the different types of users
 * and the access level each one has.
 *
 * - Subscriber - a regular user who can park and manage their orders
 * - Manager - a system administrator who can view reports and manage subscribers
 * - Attendant - a staff member who can manage subscribers
 */
public enum UserRole implements Serializable {

    /** A regular user with permission to create and manage their own parking orders */
    Subscriber,

    /** A system manager who can manage subscribers and has access to reports */
    Manager,

    /** A parking lot worker responsible for subscribers registration */
    Attendant
}
