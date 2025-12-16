package org.camunda.bpmn.getstarted.matriculacion;

import org.camunda.bpm.client.ExternalTaskClient;

public class MailWorker {

    public static void main(String[] args) {
        
        // 1. Conexión a Camunda
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(10000) 
                .build();
        
        // 2. Preparamos la herramienta de envío
        SendEmail cartero = new SendEmail();

        System.out.println("MAIL WORKER INICIADO. Esperando encargos de correo...");

        // ---------------------------------------------------------
        // SUSCRIPCIÓN 1: CORREO DE ÉXITO
        // ---------------------------------------------------------
        client.subscribe("enviar-email")
            .lockDuration(5000)
            .handler((externalTask, externalTaskService) -> {
                
                String email = externalTask.getVariable("email");
                String nombre = externalTask.getVariable("nombre");
                String matricula = externalTask.getVariable("matricula");

                System.out.println("Procesando envío de ÉXITO para: " + nombre);
                
                String cuerpo = "Hola " + nombre + ",\n\n" +
                                "Su vehículo ha sido matriculado con éxito.\n" +
                                "MATRÍCULA: " + matricula + "\n\n" +
                                "Gracias por usar nuestros servicios.";

                cartero.enviar(email, "Trámite Finalizado", cuerpo);

                externalTaskService.complete(externalTask);
            }).open();

        // ---------------------------------------------------------
        // SUSCRIPCIÓN 2: CORREO DE ERROR
        // ---------------------------------------------------------
        client.subscribe("enviar-email-error")
            .lockDuration(5000)
            .handler((externalTask, externalTaskService) -> {
                
                String email = externalTask.getVariable("email");
                
                System.out.println("Procesando aviso de ERROR para: " + email);
                
                String cuerpo = "Estimado ciudadano,\n\n" +
                                "Hay errores en su solicitud (DNI o Bastidor incorrectos).\n" +
                                "Por favor, entre en la plataforma para subsanarlos.";

                cartero.enviar(email, "URGENTE: Subsanación Requerida", cuerpo);

                externalTaskService.complete(externalTask);
            }).open();
    }
}