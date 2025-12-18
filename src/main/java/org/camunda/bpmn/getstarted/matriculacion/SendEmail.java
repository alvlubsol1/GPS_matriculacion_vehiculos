package org.camunda.bpmn.getstarted.matriculacion;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

public class SendEmail {

    // ⚠️ CAMBIA ESTO POR TUS DATOS
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 465;
    private static final String USER = "alvarolubianogps2@gmail.com"; 
    private static final String PWD = "ddhh dcnn vhaz cxtw"; 

    public void enviar(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || destinatario.isEmpty()) {
            System.out.println("ERROR: No hay destinatario.");
            return;
        }

        try {
            Email email = new SimpleEmail();
            email.setHostName(HOST);
            email.setSmtpPort(PORT);
            email.setAuthenticator(new DefaultAuthenticator(USER, PWD));
            email.setSSLOnConnect(true);
            
            email.setFrom(USER, "Sede DGT");
            email.setSubject(asunto);
            email.setMsg(mensaje);
            email.addTo(destinatario);
            
            email.send();
            
            System.out.println("CORREO ENVIADO A: " + destinatario);
            
        } catch (Exception e) {
            System.out.println("FALLO AL ENVIAR: " + e.getMessage());
        }
    }
}