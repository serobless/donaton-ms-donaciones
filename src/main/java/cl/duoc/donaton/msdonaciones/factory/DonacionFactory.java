package cl.duoc.donaton.msdonaciones.factory;

import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory Method: centraliza la construcción de Donacion según el tipo.
 * Permite aplicar reglas de negocio específicas por TipoDonacion
 * sin contaminar el Service ni el Controller.
 */
public class DonacionFactory {

    private DonacionFactory() {}

    public static Donacion crear(TipoDonacion tipo, BigDecimal monto,
                                 String alias, Causa causa) {
        return switch (tipo) {
            case MONETARIA -> buildMonetaria(monto, alias, causa);
            case ROPA      -> buildEspecie(TipoDonacion.ROPA, monto, alias, causa);
            case ALIMENTO  -> buildEspecie(TipoDonacion.ALIMENTO, monto, alias, causa);
            case MEDICA    -> buildEspecie(TipoDonacion.MEDICA, monto, alias, causa);
        };
    }

    private static Donacion buildMonetaria(BigDecimal monto, String alias, Causa causa) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de una donación monetaria debe ser mayor a 0");
        }
        return Donacion.builder()
                .tipoDonacion(TipoDonacion.MONETARIA)
                .monto(monto)
                .donanteAlias(alias)
                .causa(causa)
                .fecha(LocalDateTime.now())
                .build();
    }

    // Donaciones en especie: el monto representa valor estimado en CLP
    private static Donacion buildEspecie(TipoDonacion tipo, BigDecimal monto,
                                          String alias, Causa causa) {
        BigDecimal valorEspecie = (monto != null && monto.compareTo(BigDecimal.ZERO) > 0)
                ? monto
                : BigDecimal.ZERO;
        return Donacion.builder()
                .tipoDonacion(tipo)
                .monto(valorEspecie)
                .donanteAlias(alias)
                .causa(causa)
                .fecha(LocalDateTime.now())
                .build();
    }
}
