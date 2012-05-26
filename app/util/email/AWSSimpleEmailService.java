package util.email;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;
import exceptions.EmailException;
import play.Logger;
import play.Play;
import util.PlayUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public class AWSSimpleEmailService{
    /**
     * Sends a request to Amazon Simple Email Service to verify the specified
     * email address. This triggers a verification email, which will contain a
     * link that you can click on to complete the verification process.
     *
     * @param ses
     *            The Amazon Simple Email Service client to use when making
     *            requests to Amazon SES.
     * @param address
     *            The email address to verify.
     */
    private static void verifyEmailAddress(AmazonSimpleEmailService ses, String address)
            throws AmazonClientException {
        ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
        if (verifiedEmails.getVerifiedEmailAddresses().contains(address)) return;

        ses.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(address));
    }

    /*
     * Important: Be sure to fill in an email address you have access to
     *            so that you can receive the initial confirmation email
     *            from Amazon Simple Email Service.
     */
    private static final String FROM = PlayUtils.getApplicationConfig("email");


    public static void send(String to, String subject, String body) throws EmailException{
        URL x = Play.application().resource("AwsCredentials.properties");

        PropertiesCredentials credentials = null;
        try {
            credentials = new PropertiesCredentials(
                    Play.application().resourceAsStream("AwsCredentials.properties"));
        } catch (IOException e) {
            throw new EmailException(e);
        }

        AmazonSimpleEmailService ses = new AmazonSimpleEmailServiceClient(credentials);
        try{
            verifyEmailAddress(ses, FROM);
        } catch (AmazonClientException e){
            throw new EmailException(e);
        }

        /*
        * Setup JavaMail to use the Amazon Simple Email Service by specifying
        * the "aws" protocol.
        */
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "aws");

        /*
         * Setting mail.aws.user and mail.aws.password are optional. Setting
         * these will allow you to send mail using the static transport send()
         * convince method.  It will also allow you to call connect() with no
         * parameters. Otherwise, a user name and password must be specified
         * in connect.
         */
        props.setProperty("mail.aws.user", credentials.getAWSAccessKeyId());
        props.setProperty("mail.aws.password", credentials.getAWSSecretKey());

        Session session = Session.getInstance(props);

        try {
            // Create a new Message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            //msg.setText(body);
            msg.setContent(body, "text/html");
            msg.saveChanges();

            // Reuse one Transport object for sending all your messages
            // for better performance
            Transport t = new AWSJavaMailTransport(session, null);
            t.connect();
            t.sendMessage(msg, null);

            // Close your transport when you're completely done sending
            // all your messages
            t.close();
        } catch (AddressException e) {
            e.printStackTrace();
            Logger.error("Caught an AddressException, which means one or more of your "
                    + "addresses are improperly formatted.");
        } catch (MessagingException e) {
            e.printStackTrace();
            Logger.error("Caught a MessagingException, which means that there was a "
                    + "problem sending your message to Amazon's E-mail Service check the "
                    + "stack trace for more information.");
        }
    }
}



