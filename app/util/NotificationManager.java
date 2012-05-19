package util;


import com.avaje.ebean.Ebean;
import exceptions.QueueException;
import models.Notifications;
import play.Logger;

import javax.persistence.OptimisticLockException;

public class NotificationManager {


    /**
     * Adds an email to queue that gets peeked up by an akka job
     * running every "x" min
     * @param email
     * @param content
     * @return
     * @throws Exception
     */
    public static void queueNotification(String email, String subject, String content)
            throws QueueException{
        Notifications notification = new Notifications();
        notification.setEmail(email);
        notification.setSubject(subject);
        notification.setContent(content);
        try{
            Ebean.save(notification);
        } catch (OptimisticLockException ole){
            Logger.error("Error adding message to queue", ole.getCause());
            throw new QueueException(ole.getCause());
        }
    }
}
