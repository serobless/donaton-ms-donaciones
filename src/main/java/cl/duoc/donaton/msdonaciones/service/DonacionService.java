package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.dto.TopDonadorResponse;
import cl.duoc.donaton.msdonaciones.dto.TransparenciaResponse;
import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final CausaService causaService;

    public List<Donacion> listarTodas() {
        return donacionRepository.findAll();
    }

    @Transactional
    public Donacion crear(DonacionRequest req) {
        Causa causa = causaService.obtenerPorId(req.getCausaId());

        Donacion donacion = DonacionFactory.crear(
                req.getTipoDonacion(),
                req.getMonto() != null ? req.getMonto() : BigDecimal.ZERO,
                req.getDonanteAlias(),
                causa
        );

        Donacion guardada = donacionRepository.save(donacion);

        // Solo las donaciones monetarias suman al recaudado
        if (req.getTipoDonacion() == TipoDonacion.MONETARIA && req.getMonto() != null) {
            causaService.actualizarRecaudado(causa, req.getMonto());
        }

        return guardada;
    }

    public List<TopDonadorResponse> topDonadores() {
        return donacionRepository.findTop10Donantes().stream()
                .map(row -> new TopDonadorResponse(
                        (String) row[0],
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    public List<TransparenciaResponse> transparencia() {
        return donacionRepository.findAll().stream()
                .map(d -> TransparenciaResponse.builder()
                        .id(d.getId())
                        .donanteAlias(d.getDonanteAlias() != null ? d.getDonanteAlias() : "Anónimo")
                        .monto(d.getMonto())
                        .tipoDonacion(d.getTipoDonacion())
                        .fecha(d.getFecha())
                        .causaNombre(d.getCausa().getNombre())
                        .build()
                )
                .toList();
    }
}
