package rcpupdatesapp;

import java.util.Properties;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailer
{
    public static boolean sendMail(final String SUBJECT, final String MESSAGE, final String TOLIST)
    {   
        String[] TO = new String[1];
        TO[0] = TOLIST;
        if(!TO[0].equals("") && TO[0].contains("@") && TO[0].contains(".")){
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.user", "PIBotUpdates@gmail.com");
            props.put("mail.smtp.password", "Ifyouwantone1987");
            props.put("mail.smtp.port", 587);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session);

            try{
                mimeMessage.setFrom(new InternetAddress("PIBotUpdates@gmail.com"));
                InternetAddress[] toAddress = new InternetAddress[TO.length];

                for(int i = 0; i < TO.length; i++)
                    toAddress[i] = new InternetAddress(TO[i]);

                for(int i = 0; i < toAddress.length; i++)
                    mimeMessage.addRecipient(RecipientType.TO, toAddress[i]);

                mimeMessage.setSubject(SUBJECT);
                mimeMessage.setText(MESSAGE);
                Transport transport = session.getTransport("smtp");
                transport.connect("smtp.gmail.com", "PIBotUpdates@gmail.com", "Ifyouwantone1987");
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                transport.close();
                
                return true;
            }
            catch(MessagingException e){
                System.out.println("Error Sending Email: " + e);
            }
        }
        
        return false;
    }
}
