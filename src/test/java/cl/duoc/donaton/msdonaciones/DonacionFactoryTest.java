package cl.duoc.donaton.msdonaciones;

import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DonacionFactoryTest {

    private final Causa causa = Causa.builder()
            .id(1L).titulo("Test").meta(BigDecimal.valueOf(1000)).build();

    @Test
    void creaMonetariaConMontoPositivo() {
        Donacion d = DonacionFactory.crear(TipoDonacion.MONETARIA, BigDecimal.valueOf(50000), "alias", causa);
        assertEquals(TipoDonacion.MONETARIA, d.getTipoDonacion());
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(d.getMonto()));
    }

    @Test
    void monetariaConMontoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> DonacionFactory.crear(TipoDonacion.MONETARIA, null, "alias", causa));
    }

    @Test
    void creaRopaConMontoEnCero() {
        Donacion d = DonacionFactory.crear(TipoDonacion.ROPA, BigDecimal.ZERO, "alias", causa);
        assertEquals(TipoDonacion.ROPA, d.getTipoDonacion());
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getMonto()));
    }

    @Test
    void creaAlimentoConValorEstimado() {
        Donacion d = DonacionFactory.crear(TipoDonacion.ALIMENTO, BigDecimal.valueOf(20000), "club", causa);
        assertEquals(TipoDonacion.ALIMENTO, d.getTipoDonacion());
        assertNotNull(d.getFecha());
    }
}
