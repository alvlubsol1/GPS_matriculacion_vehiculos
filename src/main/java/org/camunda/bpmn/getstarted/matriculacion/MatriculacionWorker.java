package org.camunda.bpmn.getstarted.matriculacion;

import org.camunda.bpm.client.ExternalTaskClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MatriculacionWorker {
    
    private final static Logger LOGGER = Logger.getLogger(MatriculacionWorker.class.getName());

    public static void main(String[] args) {
        
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(10000) 
                .build();

        LOGGER.info("[INFO] MATRICULACION WORKER INICIADO. Esperando tareas...");

        // ---------------------------------------------------------
        // ROBOT 1: REGISTRAR SOLICITUD
        // ---------------------------------------------------------
        client.subscribe("registrar-solicitud")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String dni = externalTask.getVariable("dni");
                System.out.println("[SISTEMA] Registrando nueva solicitud para DNI: " + dni);
                externalTaskService.complete(externalTask);
            }).open();

        // ---------------------------------------------------------
        // ROBOT 2: REVISAR DOCUMENTACIÓN
        // ---------------------------------------------------------
        client.subscribe("revisar-auto")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String modelo = externalTask.getVariable("modelo");
                boolean docOK = true;

                // CASO DE PRUEBA 1: DOCUMENTACIÓN INCORRECTA
                // Si el modelo es "MALO", fallamos la revisión.
                if (modelo != null && modelo.equalsIgnoreCase("MALO")) {
                    docOK = false;
                    LOGGER.warning("Documentacion RECHAZADA. Motivo: Modelo incorrecto.");
                } else {
                    System.out.println(" Documentacion revisada correctamente.");
                }

                Map<String, Object> variables = new HashMap<>();
                variables.put("docOK", docOK);
                externalTaskService.complete(externalTask, variables);
            }).open();

        // ---------------------------------------------------------
        // ROBOT 3: VERIFICAR DNI
        // ---------------------------------------------------------
        client.subscribe("verificar-dni")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String dni = externalTask.getVariable("dni");
                boolean dniValido = true;

                // CASO DE PRUEBA 2: DNI FALSO
                // Si el DNI contiene "0000", simulamos que es inválido.
                if (dni != null && dni.contains("0000")) {
                    dniValido = false;
                    LOGGER.warning("DNI INVALIDO o FALSO detectado en base de datos: " + dni);
                } else {
                    System.out.println("DNI verificado correctamente.");
                }

                externalTaskService.complete(externalTask, Collections.singletonMap("dniValido", dniValido));
            }).open();

        // ---------------------------------------------------------
        // ROBOT 4: VERIFICAR BASTIDOR
        // ---------------------------------------------------------
        client.subscribe("verificar-bastidor")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String bastidor = externalTask.getVariable("bastidor");
                boolean existeBastidor = true;

                // CASO DE PRUEBA 3: BASTIDOR NO EXISTENTE
                // Si el bastidor es "ERROR", simulamos que no existe en Industria.
                if (bastidor != null && bastidor.equalsIgnoreCase("ERROR")) {
                    existeBastidor = false;
                    LOGGER.warning("El numero de bastidor NO existe en el registro: " + bastidor);
                } else {
                    System.out.println("Bastidor verificado correctamente: " + bastidor);
                }

                externalTaskService.complete(externalTask, Collections.singletonMap("existeBastidor", existeBastidor));
            }).open();

        // ---------------------------------------------------------
        // ROBOT 5: VALIDAR TASAS (PAGO)
        // ---------------------------------------------------------
        client.subscribe("validar-tasas")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String titular = externalTask.getVariable("titularPago");
                boolean tasaValida = true;

                // CASO DE PRUEBA 4: PAGO NO VÁLIDO
                // Si el titular es "POBRE", simulamos fallo en el pago.
                if (titular != null && titular.equalsIgnoreCase("POBRE")) {
                    tasaValida = false;
                    LOGGER.warning("[AEAT] PAGO RECHAZADO: Fondos insuficientes o tarjeta invalida.");
                } else {
                    System.out.println("[AEAT] Pago de tasas validado correctamente.");
                }

                externalTaskService.complete(externalTask, Collections.singletonMap("tasaValida", tasaValida));
            }).open();

        // ---------------------------------------------------------
        // ROBOT 6: EMITIR PERMISO
        // ---------------------------------------------------------
        client.subscribe("emitir-permiso")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                String matricula = externalTask.getVariable("matricula");
                System.out.println("[IMPRESION] Generando Permiso de Circulacion. Matricula: " + matricula);
                externalTaskService.complete(externalTask);
            }).open();
    }
}