package cl.duoc.donaton.msdonaciones.init;

import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.Necesidad;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import cl.duoc.donaton.msdonaciones.repository.NecesidadRepository;
import cl.duoc.donaton.msdonaciones.repository.TestimonioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CausaRepository causaRepository;
    private final CentroAcopioRepository centroAcopioRepository;
    private final DonacionRepository donacionRepository;
    private final TestimonioRepository testimonioRepository;
    private final NecesidadRepository necesidadRepository;

    @Override
    public void run(String... args) {
        // Cerrar causas vencidas o completadas al arrancar
        int vencidas    = causaRepository.cerrarVencidas(LocalDate.now());
        int completadas = causaRepository.cerrarCompletadas();
        if (vencidas > 0 || completadas > 0)
            log.info("DataInitializer: {} causa(s) vencida(s) y {} completada(s) cerrada(s) al arrancar.", vencidas, completadas);

        // Actualizar año en títulos de causas (2025 → 2026)
        int titulosFix = causaRepository.reemplazarEnTitulos("2025", "2026");
        if (titulosFix > 0) log.info("DataInitializer: {} título(s) de causa actualizados de 2025 a 2026.", titulosFix);

        // Asignar fechaInicio/fechaFin a causas que no las tengan
        causaRepository.asignarFechaInicio("Alimentación para familias vulnerables", LocalDate.of(2025, 3,  1));
        causaRepository.asignarFechaInicio("Medicamentos para adultos mayores",      LocalDate.of(2025, 4, 15));
        causaRepository.asignarFechaInicio("Abrigo para invierno 2026",              LocalDate.of(2025, 5,  1));
        causaRepository.asignarFechaInicio("Causa abrigos para pinguinos",           LocalDate.of(2025, 6,  1));

        causaRepository.asignarFechaFin("Alimentación para familias vulnerables", LocalDate.of(2025, 9, 30));
        causaRepository.asignarFechaFin("Medicamentos para adultos mayores",      LocalDate.of(2025, 12, 31));
        causaRepository.asignarFechaFin("Abrigo para invierno 2026",              LocalDate.of(2026, 8, 31));
        causaRepository.asignarFechaFin("Causa abrigos para pinguinos",           LocalDate.of(2026, 12, 31));

        // --- Centros de acopio ---
        // Corregir coordenadas inválidas de Quilicura con UPDATE directo (evita cargar lazy queRecibe)
        int quilicuraFix = centroAcopioRepository.corregirCoordenadas("Centro Quilicura", -33.3558, -70.7380);
        if (quilicuraFix > 0) log.info("DataInitializer: Coordenadas de Centro Quilicura corregidas.");

        // Corregir queRecibe de centros existentes que tengan datos incompletos
        corregirQueRecibe("Centro Quilicura",          List.of("Ropa de abrigo", "Calzado", "Frazadas"), 3);
        corregirQueRecibe("Centro Santiago Centro",    List.of("Ropa de abrigo", "Frazadas", "Calzado", "Alimentos no perecibles"), 4);
        corregirQueRecibe("Centro Valparaíso",         List.of("Ropa de abrigo", "Frazadas", "Alimentos no perecibles"), 3);
        corregirQueRecibe("Centro Concepción Biobío",  List.of("Ropa de abrigo", "Frazadas", "Alimentos no perecibles", "Medicamentos"), 4);

        java.util.Set<String> centrosExistentes = centroAcopioRepository.findAll()
                .stream().map(CentroAcopio::getNombre).collect(java.util.stream.Collectors.toSet());
        java.util.List<CentroAcopio> centrosNuevos = new java.util.ArrayList<>();
        // capacidadMax en dm³ (litros equivalentes de almacenamiento físico del centro)
        // capacidadActual parte en 0: se actualiza automáticamente al cambiar estado de donaciones
        if (!centrosExistentes.contains("Casa Central")) {
            centrosNuevos.add(CentroAcopio.builder()
                .nombre("Casa Central")
                .direccion("Av. Apoquindo 4501, Las Condes")
                .region("Metropolitana").ciudad("Santiago")
                .horario("Lun-Vie 8:00-20:00, Sáb 9:00-14:00")
                .telefono("+56 2 2700 0001")
                .queRecibe(new java.util.ArrayList<>(List.of("Ropa de abrigo", "Calzado", "Frazadas", "Alimentos no perecibles", "Medicamentos")))
                .capacidadActual(0).capacidadMax(2000)
                .unidadCapacidad("dm³")
                .latitud(-33.4172).longitud(-70.6056).build());
        }
        if (!centrosExistentes.contains("Centro Santiago Centro")) {
            centrosNuevos.add(CentroAcopio.builder()
                .nombre("Centro Santiago Centro")
                .direccion("Av. Libertador B. O'Higgins 1234")
                .region("Metropolitana").ciudad("Santiago")
                .horario("Lun-Vie 9:00-18:00, Sáb 10:00-14:00")
                .telefono("+56 2 2345 1001")
                .queRecibe(new java.util.ArrayList<>(List.of("Ropa de abrigo", "Frazadas", "Calzado", "Alimentos no perecibles")))
                .capacidadActual(0).capacidadMax(500)
                .unidadCapacidad("dm³")
                .latitud(-33.4489).longitud(-70.6693).build());
        }
        if (!centrosExistentes.contains("Centro Valparaíso")) {
            centrosNuevos.add(CentroAcopio.builder()
                .nombre("Centro Valparaíso")
                .direccion("Av. Brasil 678")
                .region("Valparaíso").ciudad("Valparaíso")
                .horario("Lun-Vie 9:00-18:00")
                .telefono("+56 32 234 5006")
                .queRecibe(new java.util.ArrayList<>(List.of("Ropa de abrigo", "Frazadas", "Alimentos no perecibles")))
                .capacidadActual(0).capacidadMax(280)
                .unidadCapacidad("dm³")
                .latitud(-33.0472).longitud(-71.6127).build());
        }
        if (!centrosExistentes.contains("Centro Concepción Biobío")) {
            centrosNuevos.add(CentroAcopio.builder()
                .nombre("Centro Concepción Biobío")
                .direccion("Av. Los Carrera 234")
                .region("Biobío").ciudad("Concepción")
                .horario("Lun-Vie 8:30-17:30")
                .telefono("+56 41 234 5008")
                .queRecibe(new java.util.ArrayList<>(List.of("Ropa de abrigo", "Frazadas", "Alimentos no perecibles", "Medicamentos")))
                .capacidadActual(0).capacidadMax(450)
                .unidadCapacidad("dm³")
                .latitud(-36.8201).longitud(-73.0444).build());
        }
        if (!centrosExistentes.contains("Centro Quilicura")) {
            centrosNuevos.add(CentroAcopio.builder()
                .nombre("Centro Quilicura")
                .direccion("Av. Manuel Antonio Matta 456")
                .region("Metropolitana").ciudad("Quilicura")
                .horario("Lun-Vie 9:00-17:00")
                .telefono("+56 2 2345 1004")
                .queRecibe(new java.util.ArrayList<>(List.of("Ropa de abrigo", "Calzado", "Frazadas")))
                .capacidadActual(0).capacidadMax(400)
                .unidadCapacidad("dm³")
                .latitud(-33.3558).longitud(-70.7380).build());
        }
        if (!centrosNuevos.isEmpty()) {
            centroAcopioRepository.saveAll(centrosNuevos);
            log.info("DataInitializer: {} centros de acopio creados.", centrosNuevos.size());
        }

        // --- Testimonios semilla (siempre verificar, independiente de causas/donaciones) ---
        if (!testimonioRepository.existsByAutorId("seed_1")) {
            testimonioRepository.saveAll(List.of(
                Testimonio.builder()
                    .titulo("Donar ropa fue más fácil de lo que pensaba")
                    .autorNombre("María Jesús González")
                    .autorId("seed_1")
                    .imagenUrl("https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=800&q=80")
                    .fechaCreacion(LocalDateTime.now().minusDays(18))
                    .contenido("<p>Llevaba años acumulando ropa que ya no usaba y siempre postergaba donarla. Un día vi el aviso de Donaton en redes sociales y decidí dar el paso.</p><p>Lo que más me sorprendió fue lo simple que fue todo: encontré el centro de acopio más cercano a mi casa en el mapa, llevé dos bolsas grandes y en menos de 10 minutos ya estaba hecho. La persona que me atendió me explicó que la ropa llegaría a familias de la comuna antes del próximo frente frío.</p><p>Desde entonces he vuelto dos veces más. Le recomendé la plataforma a mi familia y entre todos juntamos bastante para esta temporada de invierno. Pequeñas acciones que suman.</p>")
                    .build(),
                Testimonio.builder()
                    .titulo("Recibimos abrigos cuando más los necesitábamos")
                    .autorNombre("Alejandro Reyes Fuentes")
                    .autorId("seed_2")
                    .imagenUrl("https://images.unsplash.com/photo-1531983412531-1f49a365ffed?w=800&q=80")
                    .fechaCreacion(LocalDateTime.now().minusDays(12))
                    .contenido("<p>El año pasado fue muy difícil para mi familia. Después de que me quedé sin trabajo, los meses de julio y agosto se pusieron muy crudos económicamente. Vivimos en una casa antigua en Pudahuel y el frío se metía por todos lados.</p><p>Una vecina nos comentó sobre Donaton y fuimos al centro de acopio del sector. Nos entregaron frazadas, chaquetas para los niños y ropa térmica. No lo esperábamos, pero ese gesto cambió completamente cómo vivimos ese invierno.</p><p>Hoy estoy trabajando de nuevo y ya pude hacer mi primera donación. Es mi manera de devolver lo que recibí. Quiero que otra familia pase un invierno mejor gracias a esto.</p>")
                    .build(),
                Testimonio.builder()
                    .titulo("Voluntaria en el centro Santiago Centro: lo que vi me marcó")
                    .autorNombre("Valentina Mora Cisterna")
                    .autorId("seed_3")
                    .imagenUrl("https://images.unsplash.com/photo-1593113598332-cd288d649433?w=800&q=80")
                    .fechaCreacion(LocalDateTime.now().minusDays(7))
                    .contenido("<p>Me sumé como voluntaria en el Centro Santiago Centro hace tres meses, primero por cumplir horas de práctica universitaria, y me quedé porque genuinamente quise seguir.</p><p>Lo que más me llama la atención es la variedad de personas que llegan: adultos mayores solos, familias numerosas, personas en situación de calle acompañadas por monitores de organizaciones sociales. Cada historia es diferente.</p><p>Gracias a la plataforma de Donaton, el flujo de donaciones se ordenó mucho. Antes había semanas sin nada y otras en que no dábamos abasto. Ahora podemos anticipar necesidades y comunicarlas.</p><p>Si estás pensando en ser voluntario/a, te lo recomiendo sin dudarlo. No se necesita experiencia previa, solo disposición.</p>")
                    .build(),
                Testimonio.builder()
                    .titulo("Organizamos una colecta en el colegio y fue un éxito")
                    .autorNombre("Francisca Torres Álvarez")
                    .autorId("seed_4")
                    .imagenUrl("https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=800&q=80")
                    .fechaCreacion(LocalDateTime.now().minusDays(3))
                    .contenido("<p>Soy profesora de 7° básico en un colegio de Quilicura. Este año, como proyecto de formación ciudadana, propuse a mis alumnos organizar una colecta de ropa de invierno usando Donaton como plataforma.</p><p>Los chicos investigaron el mapa de necesidades, eligieron el centro más cercano y diseñaron los afiches para pegar en el colegio. En dos semanas juntamos 4 bolsas grandes de ropa en buen estado.</p><p>El día que fuimos a entregar las donaciones al Centro Quilicura, los alumnos pudieron ver en vivo cómo se reciben y clasifican las donaciones. Fue una de las mejores clases del año, sin exagerar.</p><p>Les recomiendo esta iniciativa a cualquier docente que quiera trabajar valores con proyectos concretos y medibles.</p>")
                    .build(),
                Testimonio.builder()
                    .titulo("Mi empresa se sumó y duplicamos el impacto")
                    .autorNombre("Carlos Ibáñez Vargas")
                    .autorId("seed_5")
                    .imagenUrl("https://images.unsplash.com/photo-1521791136064-7986c2920216?w=800&q=80")
                    .fechaCreacion(LocalDateTime.now().minusDays(1))
                    .contenido("<p>Trabajo en una empresa de servicios en Valparaíso con cerca de 80 personas. Propuse al área de sostenibilidad usar Donaton para organizar nuestra campaña de donación de invierno, que antes hacíamos de forma bastante desordenada.</p><p>El cambio fue inmediato. Con la plataforma pudimos ver exactamente qué necesitaba cada centro, coordinar con el equipo a través de un solo punto y hacer el seguimiento de las donaciones realizadas.</p><p>En total donamos ropa para más de 60 familias y realizamos una donación monetaria a la causa de abrigo. La transparencia del sistema fue clave para que la gerencia aprobara el presupuesto sin hesitación.</p><p>Ya estamos planeando la colecta de fin de año. Si tu empresa aún no se ha sumado, el momento es ahora.</p>")
                    .build()
            ));
            log.info("DataInitializer: 5 testimonios semilla cargados.");
        }

        if (causaRepository.count() > 0) return;

        // --- 3 Causas ---
        Causa alimentacion = causaRepository.save(Causa.builder()
                .titulo("Alimentación para familias vulnerables")
                .descripcion("Proveemos cajas de alimentos mensuales a 150 familias en situación de calle en Santiago Centro.")
                .meta(new BigDecimal("5000000"))
                .recaudado(new BigDecimal("3250000"))
                .activa(true)
                .categoria("ALIMENTACION")
                .imagenUrl("https://images.unsplash.com/photo-1593113630400-ea4288922559?w=800")
                .diasRestantes(18)
                .fechaInicio(LocalDate.of(2025, 3, 1))
                .fechaFin(LocalDate.of(2025, 9, 30))
                .build());

        Causa salud = causaRepository.save(Causa.builder()
                .titulo("Medicamentos para adultos mayores")
                .descripcion("Compra y distribución de medicamentos esenciales para adultos mayores sin previsión en la región Metropolitana.")
                .meta(new BigDecimal("2000000"))
                .recaudado(new BigDecimal("420000"))
                .activa(true)
                .categoria("SALUD")
                .imagenUrl("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800")
                .diasRestantes(45)
                .fechaInicio(LocalDate.of(2025, 4, 15))
                .fechaFin(LocalDate.of(2025, 12, 31))
                .build());

        Causa ropa = causaRepository.save(Causa.builder()
                .titulo("Abrigo para invierno 2025")
                .descripcion("Recolección y distribución de ropa de abrigo para personas en situación de calle antes del invierno.")
                .meta(new BigDecimal("1500000"))
                .recaudado(new BigDecimal("1480000"))
                .activa(true)
                .categoria("VESTIMENTA")
                .imagenUrl("https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800")
                .diasRestantes(7)
                .fechaInicio(LocalDate.of(2025, 5, 1))
                .fechaFin(LocalDate.of(2026, 8, 31))
                .build());

        log.info("Causas creadas: {}, {}, {}", alimentacion.getTitulo(), salud.getTitulo(), ropa.getTitulo());

        // --- 15 Donaciones ---
        List<Donacion> donaciones = List.of(
            buildDonacion(TipoDonacion.MONETARIA, "150000", "juanito88", alimentacion, -1),
            buildDonacion(TipoDonacion.MONETARIA, "75000",  "maria_sol", alimentacion, -3),
            buildDonacion(TipoDonacion.MONETARIA, "200000", "donador_anonimo", alimentacion, -5),
            buildDonacion(TipoDonacion.ALIMENTO,  "30000",  "club_rotario", alimentacion, -7),
            buildDonacion(TipoDonacion.ALIMENTO,  "45000",  "familia_perez", alimentacion, -10),

            buildDonacion(TipoDonacion.MONETARIA, "80000",  "carolina_v", salud, -2),
            buildDonacion(TipoDonacion.MONETARIA, "120000", "dr_rodrigo", salud, -4),
            buildDonacion(TipoDonacion.MEDICA,    "50000",  "farmacia_cruz", salud, -6),
            buildDonacion(TipoDonacion.MEDICA,    "35000",  "juanito88", salud, -8),
            buildDonacion(TipoDonacion.MONETARIA, "85000",  "maria_sol", salud, -12),

            buildDonacion(TipoDonacion.ROPA,      "0",      "centro_madre", ropa, -1),
            buildDonacion(TipoDonacion.ROPA,      "0",      "taller_costura", ropa, -3),
            buildDonacion(TipoDonacion.MONETARIA, "300000", "empresa_textil", ropa, -5),
            buildDonacion(TipoDonacion.ROPA,      "0",      "carolina_v", ropa, -9),
            buildDonacion(TipoDonacion.MONETARIA, "600000", "donador_grande", ropa, -15)
        );

        donacionRepository.saveAll(donaciones);
        log.info("DataInitializer: {} donaciones de prueba cargadas.", donaciones.size());

        // --- Necesidades por centro (solo si no existen) ---
        if (necesidadRepository.count() == 0) {
            centroAcopioRepository.findByNombre("Centro Quilicura").ifPresent(c -> necesidadRepository.saveAll(List.of(
                Necesidad.builder().centro(c).tipo("Frazadas").descripcion("Frazadas para familias en situación de calle").metaUnidades(200).unidadesActuales(85).urgente(true).diasRestantes(7).build(),
                Necesidad.builder().centro(c).tipo("Ropa").descripcion("Ropa de abrigo talla adulto y niño").metaUnidades(300).unidadesActuales(140).urgente(false).diasRestantes(21).build()
            )));
            centroAcopioRepository.findByNombre("Centro Santiago Centro").ifPresent(c -> necesidadRepository.saveAll(List.of(
                Necesidad.builder().centro(c).tipo("Alimentos").descripcion("Cajas de alimentos no perecibles").metaUnidades(150).unidadesActuales(45).urgente(true).diasRestantes(5).build(),
                Necesidad.builder().centro(c).tipo("Medicamentos").descripcion("Analgésicos, antifebriles y vendas").metaUnidades(100).unidadesActuales(60).urgente(false).build()
            )));
            centroAcopioRepository.findByNombre("Centro Valparaíso").ifPresent(c -> necesidadRepository.saveAll(List.of(
                Necesidad.builder().centro(c).tipo("Frazadas").descripcion("Frazadas dobles para temporada de frío").metaUnidades(180).unidadesActuales(130).urgente(false).diasRestantes(14).build(),
                Necesidad.builder().centro(c).tipo("Alimentos").descripcion("Leche, aceite, arroz y legumbres").metaUnidades(200).unidadesActuales(30).urgente(true).diasRestantes(3).build()
            )));
            centroAcopioRepository.findByNombre("Centro Concepción Biobío").ifPresent(c -> necesidadRepository.saveAll(List.of(
                Necesidad.builder().centro(c).tipo("Ropa").descripcion("Ropa de abrigo para el clima del sur").metaUnidades(250).unidadesActuales(80).urgente(true).diasRestantes(10).build(),
                Necesidad.builder().centro(c).tipo("Medicamentos").descripcion("Medicamentos básicos y material de curación").metaUnidades(120).unidadesActuales(20).urgente(true).diasRestantes(4).build()
            )));
            log.info("DataInitializer: necesidades de centros sembradas.");
        }
    }

    private void corregirQueRecibe(String nombreCentro, List<String> itemsEsperados, int cantidadEsperada) {
        if (centroAcopioRepository.countQueRecibeByNombre(nombreCentro) != cantidadEsperada) {
            centroAcopioRepository.clearQueRecibeByNombre(nombreCentro);
            itemsEsperados.forEach(item -> centroAcopioRepository.addQueRecibeByNombre(nombreCentro, item));
            log.info("DataInitializer: queRecibe de '{}' corregido → {}.", nombreCentro, itemsEsperados);
        }
    }

    private Donacion buildDonacion(TipoDonacion tipo, String monto, String alias,
                                    Causa causa, int diasOffset) {
        Donacion d = DonacionFactory.crear(tipo, new BigDecimal(monto), alias, causa);
        d.setFecha(LocalDateTime.now().plusDays(diasOffset));
        return d;
    }
}
