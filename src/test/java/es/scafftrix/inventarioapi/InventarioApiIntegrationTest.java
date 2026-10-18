package es.scafftrix.inventarioapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class InventarioApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listsTheSeededReferences() throws Exception {
        mockMvc.perform(get("/api/v1/referencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].sku",
                        containsInAnyOrder("TORN-M6-100", "CAJA-300", "ETI-50")));
    }

    @Test
    void movementWithoutEnoughStockReturns409() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"SALIDA","sku":"CAJA-300","almacen":"CENTRAL","cantidad":999}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
    }

    @Test
    void movementForUnknownReferenceReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"ENTRADA","sku":"NO-EXISTE","almacen":"CENTRAL","cantidad":1}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void referenceWithoutNameReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/referencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"NUEVO-SKU"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void entryRegistersAMovementWith201() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"ENTRADA","sku":"TORN-M6-100","almacen":"CENTRAL","cantidad":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));
    }

    @Test
    void stockOfAReferenceIsListedPerWarehouse() throws Exception {
        mockMvc.perform(get("/api/v1/stock/TORN-M6-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].almacen", containsInAnyOrder("CENTRAL", "NORTE")));
    }

    @Test
    void alertsContainOnlyTheReferencesBelowTheirMinimum() throws Exception {
        mockMvc.perform(get("/api/v1/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sku", containsInAnyOrder("CAJA-300", "ETI-50")));
    }

    @Test
    void movementsAreServedPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/movimientos?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.items").isArray());

        mockMvc.perform(get("/api/v1/movimientos?size=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void unknownMovementReturns404AndUnknownPathReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/movimientos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/v1/ruta-que-no-existe"))
                .andExpect(status().isNotFound());
    }
}
