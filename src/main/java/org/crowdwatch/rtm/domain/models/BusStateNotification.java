package org.crowdwatch.rtm.domain.models;

import org.crowdwatch.rtm.domain.enums.NotificationPriority;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record BusStateNotification(
    String name,
    String message,
    NotificationPriority priority,
    String centerControlOperatorCode
) {
    public static BusStateNotification of(
        String title,
        NotificationPriority priority,
        String centerControlOperatorCode,
        String messageTemplate,
        Object... templateArgs
    ) {
        return new BusStateNotification(
            title,
            String.format(messageTemplate, templateArgs),
            priority,
            centerControlOperatorCode
        );
    }

    public static BusStateNotification createBusCapacityExceededNotificationJson(
        String busCode,
        String centerControlOperatorCode
    ) {
        return of(
            "Alerta de aglomeración en bus",
            NotificationPriority.MEDIUM,
            centerControlOperatorCode,
            "El bus con código %s ha excedido su capacidad",
            busCode
        );
    }

    public static BusStateNotification createInconsistentBusCapacityDetectedAlert(
        String busCode,
        String centerControlOperatorCode
    ) {
        return of(
            "Alerta de aglomeración en bus",
            NotificationPriority.HIGH,
            centerControlOperatorCode, 
            "El bus con código %s ha reportado capacidad negativa. Posiblemente la detección de pasajeros ha fallado.",
            busCode
        );
    }

    public static BusStateNotification createRouteStateErrorAlert(
        String routeCode,
        String centerControlOperatorCode
    ) {
        return of(
            "Alerta de procesamiento fallido de estado de ruta",
            NotificationPriority.HIGH,
            centerControlOperatorCode,
            "Se identificó un error interno al escuchar el estado de aglomeración actual de la ruta con código %s", 
            routeCode
        );
    }

    public static BusStateNotification createPassengerDetectionReceivingAlert(
        String busCode,
        String centerControlOperatorCode
    ) {
        return of(
            "Alerta de procesamiento fallido de estado de bus",
            NotificationPriority.HIGH,
            centerControlOperatorCode,
            "Se identificó un error interno al procesar una detección de entrada o salida de un pasajero en el bus cuyo código es %s",
            busCode
        );
    }
}
