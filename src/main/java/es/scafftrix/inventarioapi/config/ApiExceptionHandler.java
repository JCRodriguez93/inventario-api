package es.scafftrix.inventarioapi.config;

import es.scafftrix.inventarioapi.movimientos.exception.MovimientoException;
import es.scafftrix.inventarioapi.movimientos.exception.StockInsuficienteException;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaDuplicadaException;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaException;
import org.openapitools.model.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({ReferenciaException.class, MovimientoException.class})
    public ResponseEntity<ApiError> noEncontrado(RuntimeException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler({StockInsuficienteException.class, ReferenciaDuplicadaException.class})
    public ResponseEntity<ApiError> enConflicto(RuntimeException excepcion) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> camposInvalidos(MethodArgumentNotValidException excepcion) {
        String mensaje = excepcion.getBindingResult().getFieldErrors().stream()
                .map(erroro -> erroro.getField() + " " + erroro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> peticionInvalida(Exception excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, "Petición inválida: " + excepcion.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> recursoInexistente(NoResourceFoundException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + excepcion.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> errorInterno(Exception excepcion) {
        logger.error("Error interno no controlado: {}", excepcion.getMessage(), excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + excepcion.getMessage());
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus estado, String mensaje) {
        if (estado.is4xxClientError()) {
            logger.warn("HTTP {}: {}", estado.value(), mensaje);
        }
        ApiError error = new ApiError();
        error.setStatus(estado.value());
        error.setError(estado.getReasonPhrase());
        error.setMessage(mensaje);
        return ResponseEntity.status(estado).body(error);
    }
}
