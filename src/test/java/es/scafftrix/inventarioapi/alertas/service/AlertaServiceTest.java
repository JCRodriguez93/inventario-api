package es.scafftrix.inventarioapi.alertas.service;

import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.Alerta;
import org.openapitools.model.Referencia;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private StockConsulta stock;

    @InjectMocks
    private AlertaService service;

    @Test
    void onlyReferencesBelowTheirMinimumAreReturned() {
        Referencia sana = new Referencia().sku("OK-1").nombre("Sana").stockMinimo(10);
        Referencia baja = new Referencia().sku("BAJA-1").nombre("Baja").stockMinimo(50);

        when(stock.referencias()).thenReturn(List.of(sana, baja));
        when(stock.stockTotal("OK-1")).thenReturn(100);
        when(stock.stockTotal("BAJA-1")).thenReturn(12);

        List<Alerta> alertas = service.findAll();

        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getSku()).isEqualTo("BAJA-1");
        assertThat(alertas.get(0).getStockTotal()).isEqualTo(12);
        assertThat(alertas.get(0).getStockMinimo()).isEqualTo(50);
    }

    @Test
    void returnsNothingWhenEveryReferenceIsAboveItsMinimum() {
        Referencia sana = new Referencia().sku("OK-1").nombre("Sana").stockMinimo(10);

        when(stock.referencias()).thenReturn(List.of(sana));
        when(stock.stockTotal("OK-1")).thenReturn(100);

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoReferences() {
        when(stock.referencias()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }
}
