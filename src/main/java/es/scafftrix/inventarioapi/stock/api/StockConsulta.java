package es.scafftrix.inventarioapi.stock.api;

import org.openapitools.model.NivelStock;
import org.openapitools.model.Referencia;

import java.util.List;

/**
 * API pública del módulo stock: el resto de módulos solo pueden consultar y
 * mover stock a través de esta interfaz, nunca tocando sus entidades.
 */
public interface StockConsulta {

    List<Referencia> referencias();

    Referencia referencia(String sku);

    List<NivelStock> stockPorSku(String sku);

    int stockTotal(String sku);

    void aplicarMovimiento(String sku, String almacen, int delta);
}
