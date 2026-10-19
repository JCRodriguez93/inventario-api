package es.scafftrix.inventarioapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventarioApiApplication {

    private static final Logger logger = LoggerFactory.getLogger(InventarioApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InventarioApiApplication.class, args);
        logger.info("inventario-api iniciada correctamente");
    }
}
