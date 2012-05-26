package jobs;

import akka.util.Duration;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxRunnable;
import com.avaje.ebean.TxScope;
import exceptions.EmailException;
import models.Notifications;
import play.Logger;
import play.libs.Akka;
import util.email.AWSSimpleEmailService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reads emails from database ( Entity models.Notifications ) and
 * tries to send them using AWS email service
 *
 * TODO: implement some kind of distribution according to email delivery policies:
 *       For instance, no more then 100 emails each time
 * TODO: make sure one batch as finished before starting a new one
 * TODO: retry count: try to send a notification for a max number of times before give up;
 */
public class NotificationDispatcher implements Job {

    @Override
    public void run() {

        // Starts an Akka job running
        // every 1 minute
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS),
                Duration.create(1, TimeUnit.MINUTES),
                new Runnable() {
                    @Override
                    public synchronized void run() {

                        // Create a transaction where objects read are locked
                        // until commited (at least, that's my understanding of
                        // TxIsolation.READ_COMMITED )
                        TxScope txScope = TxScope.requiresNew()
                                .setIsolation(TxIsolation.READ_COMMITED)
                                .setNoRollbackFor(IOException.class);

                        // Execute transaction: read pending notifications and send email
                        Ebean.execute(txScope, new TxRunnable() {

                            public void run() {
                                List<Notifications> pendingNotifications = Notifications.find.where()
                                        .disjunction()
                                        .eq("state", Notifications.State.NEW)
                                        .eq("state", Notifications.State.FAILED)
                                        .endJunction()
                                        .orderBy("id")
                                        .findList();

                                for (Notifications n : pendingNotifications) {
                                    try {
                                        AWSSimpleEmailService.send(n.getEmail(), n.getSubject(), n.getContent());
                                        n.setState(Notifications.State.SENT);
                                        Ebean.update(n);
                                    } catch (EmailException e) {
                                        Logger.error("Error sending email for:" + n.getEmail(), e);
                                        n.setState(Notifications.State.FAILED);
                                        Ebean.update(n);
                                    }
                                }
                            }
                        });
                    }
                });
    }
}
