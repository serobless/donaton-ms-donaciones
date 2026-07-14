package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.dto.ItemDonacionRequest;
import cl.duoc.donaton.msdonaciones.dto.TopDonadorResponse;
import cl.duoc.donaton.msdonaciones.dto.TransparenciaResponse;
import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.EstadoDonacion;
import cl.duoc.donaton.msdonaciones.model.ItemDonacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import cl.duoc.donaton.msdonaciones.repository.ItemDonacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final ItemDonacionRepository itemDonacionRepository;
    private final CausaService causaService;
    private final CausaRepository causaRepository;
    private final CentroAcopioRepository centroAcopioRepository;

    public List<Donacion> listarTodas() {
        return donacionRepository.findAll();
    }

    private static final BigDecimal LIMITE_DONACION = new BigDecimal("3000000");

    @Transactional
    public Donacion crear(DonacionRequest req, String donadorId) {
        // Validación de monto máximo
        if (req.getTipoDonacion() == TipoDonacion.MONETARIA && req.getMonto() != null) {
            if (req.getMonto().compareTo(LIMITE_DONACION) > 0) {
                boolean esEmpresa = Boolean.TRUE.equals(req.getEsEmpresa());
                if (!esEmpresa) {
                    throw new IllegalArgumentException(
                        "Las donaciones monetarias tienen un límite de $3.000.000. " +
                        "Para montos mayores, regístrate como empresa y la donación quedará pendiente de aprobación."
                    );
                }
                // Empresa puede donar más, pero requiere aprobación manual
                req.setDonanteAlias(req.getNombreEmpresa() != null ? req.getNombreEmpresa() : req.getDonanteAlias());
            }
        }

        Causa causa = causaService.obtenerPorId(req.getCausaId());

        Donacion donacion = DonacionFactory.crear(
                req.getTipoDonacion(),
                req.getMonto() != null ? req.getMonto() : BigDecimal.ZERO,
                req.getDonanteAlias(),
                causa
        );
        donacion.setDonadorId(donadorId);
        donacion.setDescripcion(req.getDescripcion());
        donacion.setCantidad(req.getCantidad());
        donacion.setUnidad(req.getUnidad());
        donacion.setDireccionDonante(req.getDireccionDonante());
        if (req.getEsEmpresa() != null) donacion.setEsEmpresa(req.getEsEmpresa());
        donacion.setNombreEmpresa(req.getNombreEmpresa());

        // Donación empresa > $3M: requiere aprobación antes de contabilizarse
        boolean montoGrande = req.getMonto() != null && req.getMonto().compareTo(LIMITE_DONACION) > 0;
        if (Boolean.TRUE.equals(req.getEsEmpresa()) && montoGrande) {
            donacion.setRequiereAprobacion(true);
        }

        // Resolver el centro de acopio (explícito o Casa Central por defecto)
        CentroAcopio centroAsignado = req.getCentroAcopioId() != null
                ? centroAcopioRepository.findById(req.getCentroAcopioId()).orElse(null)
                : centroAcopioRepository.findByNombre("Casa Central").orElse(null);

        if (centroAsignado != null) {
            donacion.setCentroAcopio(centroAsignado);
            if (req.getTipoDonacion() != TipoDonacion.MONETARIA
                    && req.getCantidad() != null && req.getCantidad() > 0) {
                validarCapacidadCentro(centroAsignado, req.getTipoDonacion(), req.getCantidad());
                centroAcopioRepository.incrementarCapacidad(centroAsignado.getId(), req.getCantidad());
            }
        }

        Donacion guardada = donacionRepository.save(donacion);

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (ItemDonacionRequest itemReq : req.getItems()) {
                itemDonacionRepository.save(ItemDonacion.builder()
                        .donacion(guardada)
                        .descripcion(itemReq.getDescripcion())
                        .cantidad(itemReq.getCantidad())
                        .unidad(itemReq.getUnidad())
                        .build());
            }
        }

        // Solo suman al recaudado si no requieren aprobación previa
        if (req.getTipoDonacion() == TipoDonacion.MONETARIA && req.getMonto() != null
                && !Boolean.TRUE.equals(donacion.getRequiereAprobacion())) {
            causaService.actualizarRecaudado(causa, req.getMonto());
        }

        return guardada;
    }

    public List<TopDonadorResponse> topDonadores(int limit) {
        return donacionRepository.findTopDonantes(limit).stream()
                .map(row -> new TopDonadorResponse(
                        (String) row[0],
                        new BigDecimal(row[1].toString()),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    public List<Donacion> listarPorDonador(String donadorId) {
        if (donadorId == null || donadorId.isBlank()) return List.of();
        return donacionRepository.findByDonadorId(donadorId);
    }

    public List<Donacion> listarPorCentro(Long centroId) {
        return donacionRepository.findByCentroAcopioIdOrderByFechaDesc(centroId);
    }

    public List<Donacion> listarUltimas(int limit) {
        return donacionRepository.findAllByOrderByFechaDesc(PageRequest.of(0, limit));
    }

    public BigDecimal totalRecaudado() {
        return donacionRepository.sumTotalMontos();
    }

    public long conteo() {
        return donacionRepository.count();
    }

    // Espacio equivalente en dm³ (litros) por artículo según tipo de donación
    private static final java.util.Map<TipoDonacion, Integer> ESPACIO_POR_TIPO = java.util.Map.of(
        TipoDonacion.ROPA,      5,   // una prenda doblada ≈ 5 dm³
        TipoDonacion.ALIMENTO,  3,   // una bolsa/caja de alimentos ≈ 3 dm³
        TipoDonacion.MEDICA,    1,   // un paquete de medicamentos ≈ 1 dm³
        TipoDonacion.MONETARIA, 0    // dinero no ocupa espacio físico
    );

    private int calcularEspacio(Donacion d) {
        int factor = ESPACIO_POR_TIPO.getOrDefault(d.getTipoDonacion(), 1);
        int cantidad = d.getCantidad() != null && d.getCantidad() > 0 ? d.getCantidad() : 1;
        return factor * cantidad;
    }

    private void validarCapacidadCentro(CentroAcopio centro, TipoDonacion tipo, int cantidad) {
        int factor = ESPACIO_POR_TIPO.getOrDefault(tipo, 1);
        int espacioNecesario = factor * cantidad;
        int actual = centro.getCapacidadActual() != null ? centro.getCapacidadActual() : 0;
        int max = centro.getCapacidadMax() != null ? centro.getCapacidadMax() : 0;
        int disponible = max - actual;
        if (espacioNecesario > disponible) {
            throw new IllegalArgumentException(
                "El centro '" + centro.getNombre() + "' no tiene espacio suficiente. " +
                "Espacio disponible: " + disponible + " dm³, " +
                "tu donación necesita: " + espacioNecesario + " dm³."
            );
        }
    }

    @Transactional
    public Donacion actualizarEstado(Long id, EstadoDonacion nuevoEstado) {
        Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Donación no encontrada: " + id));

        EstadoDonacion estadoAnterior = donacion.getEstado();
        donacion.setEstado(nuevoEstado);

        // Auto-actualiza la capacidad del centro según la transición de estado
        if (donacion.getCentroAcopio() != null) {
            int espacio = calcularEspacio(donacion);
            if (espacio > 0) {
                centroAcopioRepository.findById(donacion.getCentroAcopio().getId()).ifPresent(centro -> {
                    int actual = centro.getCapacidadActual() != null ? centro.getCapacidadActual() : 0;
                    if (estadoAnterior == EstadoDonacion.PENDIENTE && nuevoEstado == EstadoDonacion.EN_PROCESO) {
                        // La donación entra físicamente al centro
                        centro.setCapacidadActual(actual + espacio);
                    } else if (estadoAnterior == EstadoDonacion.EN_PROCESO &&
                               (nuevoEstado == EstadoDonacion.COMPLETADA || nuevoEstado == EstadoDonacion.CANCELADA)) {
                        // La donación sale del centro (distribuida o cancelada)
                        centro.setCapacidadActual(Math.max(0, actual - espacio));
                    }
                    centroAcopioRepository.save(centro);
                });
            }
        }

        return donacionRepository.save(donacion);
    }

    @Transactional
    public Donacion aprobar(Long id) {
        Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Donación no encontrada: " + id));
        if (!Boolean.TRUE.equals(donacion.getRequiereAprobacion())) {
            throw new IllegalArgumentException("Esta donación no requiere aprobación.");
        }
        donacion.setRequiereAprobacion(false);
        // Ahora sí se suma al recaudado
        if (donacion.getTipoDonacion() == TipoDonacion.MONETARIA && donacion.getMonto() != null) {
            causaService.actualizarRecaudado(donacion.getCausa(), donacion.getMonto());
            // Cerrar la causa inmediatamente si ya alcanzó o superó la meta
            causaRepository.cerrarCompletadas();
        }
        return donacionRepository.save(donacion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!donacionRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Donación no encontrada: " + id);
        }
        donacionRepository.deleteById(id);
    }

    public List<TransparenciaResponse> transparencia() {
        return donacionRepository.findAll().stream()
                .map(d -> TransparenciaResponse.builder()
                        .id(d.getId())
                        .donadorNombre(d.getDonanteAlias() != null ? d.getDonanteAlias() : "Anónimo")
                        .monto(d.getMonto())
                        .tipoDonacion(d.getTipoDonacion())
                        .fecha(d.getFecha())
                        .causaNombre(d.getCausa().getTitulo())
                        .descripcion(d.getDescripcion())
                        .esEmpresa(Boolean.TRUE.equals(d.getEsEmpresa()))
                        .nombreEmpresa(d.getNombreEmpresa())
                        .requiereAprobacion(Boolean.TRUE.equals(d.getRequiereAprobacion()))
                        .build()
                )
                .toList();
    }
}
