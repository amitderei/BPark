package common;

/**
 * This enum is the status of order:
 * FULFILLED- the subscriber used the order to get in to the lot in the date and time of order.
 * ACTIVE-  the scheduled date and time of the order have not yet passed
 * INACTIVE- the date and time of order pass
 * CANCELLED-the subscriber cancel the order
 */
public enum StatusOfOrder {
	FULFILLED,
	ACTIVE,
	INACTIVE,
	CANCELLED
}
