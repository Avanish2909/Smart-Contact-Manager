package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	//this is responsible to send email...
    public static boolean sendEmail(String message,String subject,String to) {
    	//rest of the code
    	
    	boolean f = false;
    	
    	String from = "avanish9192tech@gmail.com";
    	
    	
    	//variable for gmail
    	String host = "smtp.gmail.com";
    	
    	//get the system properties
    	Properties properties = System.getProperties();
    	System.out.println("PROPERTIES"+properties);
    	
    	//setting important information to properties object
    	
    	//host set
    	properties.put("mail.smtp.host", host);
    	properties.put("mail.smtp.port", "465");		//google port:--> 465
    	properties.put("mail.smtp.ssl.enable", "true");
    	properties.put("mail.smtp.auth", "true");
    	
    	
    	//Steo 1 : to get the session object...
    	Session session = Session.getInstance(properties, new Authenticator() {

			
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("avanish9192tech@gmail.com", "gnjhgiwbfvrlsztv");
			}
    		
    	});
    	
    	session.setDebug(true);
    	
    	//Step 2 : compose the message [text,multi media]
    	MimeMessage m = new MimeMessage(session);
    	
    	try {
    		//from email
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
//			m.setText(message);
			m.setContent(message,"text/html");
			
			//send
			
			//Step 3 : send the message using transport class
			Transport.send(m);
			
			System.out.println("Sent success............");
			
			f = true;
			
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return f;
    	
    }
}
