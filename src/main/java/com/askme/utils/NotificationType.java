
package main.java.com.askme.utils;

public enum NotificationType {
	DUMMY(0), NOTIF_QS(1), NOTIF_ANS(2), NOTIF_UPVOTE(3), NOTIF_FOLLOW(4);

	final private int notificationType;

	NotificationType(int notificationType)
	{
		this.notificationType = notificationType;
	}

	public int getNotificationType()
	{
		return notificationType;
	}

	public static NotificationType getNotificationType(int index)
	{
		switch(index)
		{
			case 0:
				return DUMMY;

			case 1:
				return NOTIF_QS;

			case 2:
				return NOTIF_ANS;

			case 3:
				return NOTIF_UPVOTE;

			case 4:
				return NOTIF_FOLLOW;

			default:
				return null;
		}
	}

	public static String getNotificationTitle(NotificationType type)
	{
		switch(type)
		{
			case DUMMY:
				return "No notifications.";

			case NOTIF_QS:
				return "New question(s) available in Topic.";

			case NOTIF_ANS:
				return "Yay! Your question(s) has been answered.";

			case NOTIF_UPVOTE:
				return "Woohoo! Your answer has been up-voted.";

			case NOTIF_FOLLOW:
				return "Your question is trending..";

			default:
				return null;
		}
	}

	public String getTypeString()
	{
		return this.name();
	}
}
