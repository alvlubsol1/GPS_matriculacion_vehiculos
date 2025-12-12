package org.camunda.bpmn.getstarted.matriculacion;

import org.camunda.bpm.client.ExternalTaskClient;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Map;

public class MatriculacionVehiculos {
    // Logger para ver qué pasa en la consola
    private final static Logger LOGGER = Logger.getLogger(MatriculacionVehiculos.class.getName());

    public static void main(String[] args) {
        
        // 1. Configuración: Le decimos al robot dónde está Camunda
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(10000) // Espera larga para no saturar
                .build();

        // ---------------------------------------------------------
        // ROBOT 1: VERIFICAR DNI
        // Escucha el topic que pusiste en el Modeler: "verificar-dni"
        // ---------------------------------------------------------
        client.subscribe("verificar-dni")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                LOGGER.info("🤖 ROBOT: Verificando DNI del ciudadano...");
                
                // Aquí simularíamos la conexión con la Policía
                // Le decimos a Camunda que todo está OK y creamos la variable dniValido
                Map<String, Object> variables = Collections.singletonMap("dniValido", true);
                
                externalTaskService.complete(externalTask, variables);
                LOGGER.info("DNI Validado. Avanzando...");
            })
            .open();

        // ---------------------------------------------------------
        // ROBOT 2: VERIFICAR BASTIDOR
        // Topic: "verificar-bastidor"
        // ---------------------------------------------------------
        client.subscribe("verificar-bastidor")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                LOGGER.info("ROBOT: Comprobando bastidor en base de datos...");
                
                Map<String, Object> variables = Collections.singletonMap("bastidorExiste", true);
                
                externalTaskService.complete(externalTask, variables);
                LOGGER.info("Bastidor encontrado. Avanzando...");
            })
            .open();

        // ---------------------------------------------------------
        // ROBOT 3: VERIFICAR PAGO TASAS
        // Topic: "verificar-tasas"
        // ---------------------------------------------------------
        client.subscribe("verificar-tasas")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                LOGGER.info("ROBOT: Verificando pago en el banco...");
                
                // IMPORTANTE: Aquí NO necesitamos enviar "documentacionCorrecta"
                // porque lo configuraste en el Input/Output del Modeler antes.
                // Simplemente completamos la tarea.
                externalTaskService.complete(externalTask);
                
                LOGGER.info("Tasas pagadas. Pasando el control al humano (si aplica) o finalizando.");
            })
            .open();
            
        LOGGER.info("SISTEMA ARRANCADO: Esperando solicitudes de matriculación...");
    }
}
