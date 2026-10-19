package es.scafftrix.inventarioapi.config;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import es.scafftrix.inventarioapi.stock.entity.StockEntity;
import es.scafftrix.inventarioapi.stock.repository.ReferenciaRepository;
import es.scafftrix.inventarioapi.stock.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Siembra datos de ejemplo la primera vez que arranca con una base vacía.
 * Alimenta la demo (GET /alertas, POST /movimientos con 409) sin depender
 * de scripts SQL con dialecto propio.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final ReferenciaRepository referencias;
    private final StockRepository stock;

    public DemoDataSeeder(ReferenciaRepository referencias, StockRepository stock) {
        this.referencias = referencias;
        this.stock = stock;
    }

    @Override
    public void run(String... args) {
        if (referencias.count() > 0) {
            logger.debug("Base con datos: siembra de ejemplo omitida");
            return;
        }
        logger.info("Base vacía detectada: sembrando datos de ejemplo");
        guardarReferencia("TORN-M6-100", "Tornillo M6 x 100", 100);
        guardarReferencia("CAJA-300", "Caja de cartón 300x200", 50);
        guardarReferencia("ETI-50", "Etiqueta adhesiva (pack 50)", 200);

        guardarStock("TORN-M6-100", "CENTRAL", 250);
        guardarStock("TORN-M6-100", "NORTE", 40);
        guardarStock("CAJA-300", "CENTRAL", 12);
        logger.info("Datos de ejemplo listos: {} referencias, {} filas de stock",
                referencias.count(), stock.count());
    }

    private void guardarReferencia(String sku, String nombre, int stockMinimo) {
        ReferenciaEntity entidad = new ReferenciaEntity();
        entidad.setSku(sku);
        entidad.setNombre(nombre);
        entidad.setStockMinimo(stockMinimo);
        referencias.save(entidad);
    }

    private void guardarStock(String sku, String almacen, int cantidad) {
        StockEntity entidad = new StockEntity();
        entidad.setSku(sku);
        entidad.setAlmacen(almacen);
        entidad.setCantidad(cantidad);
        stock.save(entidad);
    }
}
