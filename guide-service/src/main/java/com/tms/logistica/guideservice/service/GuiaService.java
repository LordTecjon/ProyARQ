package com.tms.logistica.guideservice.service;

import com.tms.logistica.guideservice.domain.port.DocumentoPdfPort;
import com.tms.logistica.guideservice.domain.port.IdentidadValidadorPort;
import com.tms.logistica.guideservice.domain.port.SunatOsePort;
import com.tms.logistica.guideservice.domain.valueobject.InfoDni;
import com.tms.logistica.guideservice.domain.valueobject.InfoRuc;
import com.tms.logistica.guideservice.domain.valueobject.ResultadoEnvioSunat;
import com.tms.logistica.guideservice.exception.GuiaException;
import com.tms.logistica.guideservice.model.dto.request.CrearGuiaRequest;
import com.tms.logistica.guideservice.model.dto.request.FiltroGuiaRequest;
import com.tms.logistica.guideservice.model.dto.response.GuiaResponse;
import com.tms.logistica.guideservice.model.dto.response.PaginaResponse;
import com.tms.logistica.guideservice.model.entity.AuditoriaGuia;
import com.tms.logistica.guideservice.model.entity.GuiaDetalle;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import com.tms.logistica.guideservice.model.enums.EstadoGuia;
import com.tms.logistica.guideservice.repository.AuditoriaGuiaRepository;
import com.tms.logistica.guideservice.repository.GuiaRemisionRepository;
import com.tms.logistica.guideservice.util.GuiaMapper;
import com.tms.logistica.guideservice.util.NumeroGuiaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GuiaService - Logica de negocio del Modulo 3
 *
 * este servicio coordina el ciclo de vida completo de una Guia de Remision
 * Electronica: creacion, validacion de datos contra APIs externas, envio a
 * SUNAT via el OSE de APIsPerú, consulta de estado, anulacion y descarga de PDF.
 *
 * PATRÓN Anti-Corruption Layer aplicado:
 *   Este servicio depende ÚNICAMENTE de interfaces (ports) del dominio.
 *   No conoce ningún detalle de implementación externa:
 *
 *   - SunatOsePort         → envía la GRE al OSE (implementado por SunatOseAclAdapter)
 *   - IdentidadValidadorPort → valida RUC/DNI (implementado por ApisPeruIdentidadAdapter)
 *   - DocumentoPdfPort     → genera el PDF (implementado por ITextPdfAdapter)
 *
 *   Los value objects del dominio (ResultadoEnvioSunat, InfoRuc, InfoDni)
 *   reemplazan los inner records que antes "contaminaban" el dominio con
 *   conceptos específicos de APIsPerú (CdrResult, RucInfo, DniInfo).
 *
 * Estrategia de resiliencia:
 *   Las validaciones de RUC y DNI son de mejor esfuerzo: si el port falla,
 *   se registra un warning y se continua con los datos manuales.
 *   El envio a SUNAT tiene manejo de errores: si falla la conexion,
 *   la guia queda en PENDIENTE y el scheduler la reintenta cada 5 minutos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuiaService {

    // repositorio principal de guias de remision
    private final GuiaRemisionRepository   guiaRepo;
    // repositorio para registrar todos los eventos de auditoria del modulo
    private final AuditoriaGuiaRepository  auditoriaRepo;
    // genera el siguiente numero correlativo de forma sincronizada
    private final NumeroGuiaGenerator      numeroGenerator;
    // constructor del xml de la gre segun el esquema xsd oficial de sunat
    private final GREBuilder               greBuilder;

    // ── Ports del ACL (interfaces del dominio — no implementaciones concretas) ──

    /** Puerto de salida: envía la GRE al OSE/SUNAT. */
    private final SunatOsePort             sunatOsePort;

    /** Puerto de salida: valida RUC contra SUNAT y DNI contra RENIEC. */
    private final IdentidadValidadorPort   identidadPort;

    /** Puerto de salida: genera el PDF oficial de la GRE aceptada. */
    private final DocumentoPdfPort         documentoPdfPort;

    // RF3.1: Crear guia de remision electronica
    /**
     * crea una nueva Guia de Remision Electronica en estado pendiente.
     *
     * Proceso:
     *  1. valida ruc del remitente y destinatario via identidadPort (ACL)
     *  2. valida dni del conductor y actualiza el nombre oficial si el port responde
     *  3. genera el numero de serie y correlativo siguiente
     *  4. persiste la guia con todos sus detalles en mysql
     *  5. registra el evento de creacion en la tabla de auditoria
     *
     * @param "request" datos del formulario de la guia — validados con @Valid en el controller
     * @param "usuario" identificador del usuario que ejecuta la accion
     * @return GuiaResponse con los datos de la guia creada y su uuid
     */
    @Transactional
    public GuiaResponse crearGuia(CrearGuiaRequest request, String usuario) {
        log.info("Creando guía para orden {} por usuario {}", request.getOrdenId(), usuario);

        // validaciones contra sistemas externos — via ports del ACL (best-effort)
        validarRuc(request.getRemitenteRuc(), "remitente");
        validarRuc(request.getDestinatarioRuc(), "destinatario");
        validarDni(request.getConductorDni(), request);

        // genera serie y correlativo: siempre usa la serie T001 para guias de traslado
        String[] numero = numeroGenerator.siguiente("T001");

        // construccion de la entidad con el patron builder de lombok
        GuiaRemision guia = GuiaRemision.builder()
                .uuid(UUID.randomUUID().toString())
                .serie(numero[0])
                .correlativo(numero[1])
                .ordenId(request.getOrdenId())
                .motivoTraslado(request.getMotivoTraslado())
                .modalidad(request.getModalidad())
                .fechaInicio(request.getFechaInicio())
                .remitenteRuc(request.getRemitenteRuc())
                .remitenteRazon(request.getRemitenteRazon())
                .remitenteDir(request.getRemitenteDir())
                .remitenteUbigeo(request.getRemitenteUbigeo())
                .destinatarioRuc(request.getDestinatarioRuc())
                .destinatarioRazon(request.getDestinatarioRazon())
                .destinatarioDir(request.getDestinatarioDir())
                .destinatarioUbigeo(request.getDestinatarioUbigeo())
                .destinoDir(request.getDestinoDir())
                .destinoUbigeo(request.getDestinoUbigeo())
                .vehiculoPlaca(request.getVehiculoPlaca())
                .conductorDni(request.getConductorDni())
                .conductorNombre(request.getConductorNombre())
                .conductorLicencia(request.getConductorLicencia())
                .creadoPor(usuario)
                .build();

        // agrega los detalles de bienes con numero de item secuencial
        // AtomicInteger se usa para poder modificar el contador dentro del lambda
        AtomicInteger itemCounter = new AtomicInteger(1);
        request.getDetalles().forEach(d -> {
            GuiaDetalle detalle = GuiaDetalle.builder()
                    .guia(guia)
                    .item(itemCounter.getAndIncrement())
                    .descripcion(d.getDescripcion())
                    .unidadMedida(d.getUnidadMedida())
                    .cantidad(d.getCantidad())
                    .pesoBrutoKg(d.getPesoBrutoKg())
                    .build();
            guia.getDetalles().add(detalle);
        });

        // persiste la guia y sus detalles en una sola transaccion
        GuiaRemision guardada = guiaRepo.save(guia);

        // registra el evento de creacion para auditoria
        registrarAuditoria(guardada.getId(), "GENERAR", AuditoriaGuia.Resultado.OK,
                "guia " + guardada.getSerie() + "-" + guardada.getCorrelativo() + " creada",
                usuario, null);

        return GuiaMapper.toResponse(guardada);
    }

    // RF3.3: Enviar guia a SUNAT
    /**
     * envia la guia al ose y actualiza su estado segun la respuesta del CDR.
     *
     * solo se puede enviar una guia en estado pendiente.
     * mientras dura la llamada al ose el estado cambia a EN_PROCESO.
     * al terminar el estado queda ACEPTADA o RECHAZADA segun el CDR de SUNAT.
     *
     * Si hay un error de red, la guia vuelve a PENDIENTE con un proximo_reenvio
     * programado para 5 minutos despues. El scheduler la reintentara automaticamente.
     *
     * PATRÓN ACL: el resultado llega como ResultadoEnvioSunat (value object del dominio),
     * no como un tipo externo de APIsPerú.
     *
     * @param "uuid" UUID unico de la guia a enviar
     * @param "usuario" Usuario que ejecuta la accion
     * @return GuiaResponse con el estado final y el CDR de SUNAT
     */
    @Transactional
    public GuiaResponse enviarASunat(String uuid, String usuario) {
        GuiaRemision guia = obtenerEntidad(uuid);

        // solo se puede enviar una guia que este en estado PENDIENTE
        if (guia.getEstado() != EstadoGuia.PENDIENTE) {
            throw GuiaException.estadoInvalido(guia.getEstado().name(), "enviar a sunat");
        }

        // marca EN_PROCESO para indicar que el envio esta en curso
        guia.setEstado(EstadoGuia.EN_PROCESO);

        try {
            // Invoca el port del ACL — GuiaService no conoce SunatOseAclAdapter
            ResultadoEnvioSunat resultado = sunatOsePort.enviar(guia);

            // persiste el CDR: el dominio trabaja con value objects, no con JSON crudo
            guia.setCdrCodigo(resultado.codigoCdr());
            guia.setCdrDescripcion(resultado.descripcionCdr());
            guia.setCdrResponse(resultado.rawResponse());

            // el flag aceptada() simplifica la lógica de negocio
            guia.setEstado(resultado.aceptada() ? EstadoGuia.ACEPTADA : EstadoGuia.RECHAZADA);
            guia.setIntentosEnvio(guia.getIntentosEnvio() + 1);

            registrarAuditoria(guia.getId(), "ENVIAR_SUNAT", AuditoriaGuia.Resultado.OK,
                    "CDR código: " + resultado.codigoCdr(), usuario, null);

        } catch (Exception ex) {
            // error de red: encola para reenvio automatico en 5 minutos
            log.warn("Fallo al enviar guía {} a SUNAT: {}", uuid, ex.getMessage());
            guia.setEstado(EstadoGuia.PENDIENTE);
            guia.setIntentosEnvio(guia.getIntentosEnvio() + 1);
            guia.setProximoReenvio(LocalDateTime.now().plusMinutes(5));

            registrarAuditoria(guia.getId(), "ENVIAR_SUNAT", AuditoriaGuia.Resultado.ERROR,
                    ex.getMessage(), usuario, null);
        }

        return GuiaMapper.toResponse(guiaRepo.save(guia));
    }

    // RF3.7: Anular guia
    /**
     * anula una guia de remision registrando el motivo y el usuario responsable.
     * no se puede anular una guia que ya esta anulada.
     *
     * @param "uuid" UUID de la guia a anular
     * @param "motivo" descripcion del motivo de anulacion
     * @param "usuario" usuario que ejecuta la anulacion
     */
    @Transactional
    public GuiaResponse anularGuia(String uuid, String motivo, String usuario) {
        GuiaRemision guia = obtenerEntidad(uuid);

        // no tiene sentido anular una guia que ya fue anulada
        if (guia.getEstado() == EstadoGuia.ANULADA) {
            throw GuiaException.estadoInvalido("ANULADA", "anular");
        }

        guia.setEstado(EstadoGuia.ANULADA);
        guia.setAnuladoEn(LocalDateTime.now());
        guia.setAnuladoPor(usuario);
        guia.setMotivoAnulacion(motivo);

        registrarAuditoria(guia.getId(), "ANULAR", AuditoriaGuia.Resultado.OK,
                "Motivo: " + motivo, usuario, null);

        return GuiaMapper.toResponse(guiaRepo.save(guia));
    }

    // RF3.4: Consultar estado de la guia
    /**
     * retorna el estado actual de una guia por su UUID.
     * consulta el estado guardado en la bd local.
     *
     * @param "uuid" UUID unico de la guia
     * @return GuiaResponse con todos los datos y el estado actual
     */
    @Transactional(readOnly = true)
    public GuiaResponse obtenerPorUuid(String uuid) {
        return GuiaMapper.toResponse(obtenerEntidad(uuid));
    }

    // RF3.6: Listar guias por orden
    /**
     * lista todas las guias asociadas a una orden de transporte.
     * una orden puede tener multiples guias.
     *
     * @param "ordenId" ID de la orden de transporte
     * @return lista de guias asociadas a esa orden
     */
    @Transactional(readOnly = true)
    public List<GuiaResponse> listarPorOrden(Long ordenId) {
        return guiaRepo.findByOrdenId(ordenId).stream()
                .map(GuiaMapper::toResponse)
                .toList();
    }

    // RF3.8: Historial con filtros y paginacion
    /**
     * retorna el historial de guias con filtros opcionales y paginacion.
     * soporta filtrar por: rango de fechas, estado y ordenId.
     * los resultados se ordenan por fecha de creacion descendente.
     *
     * @param "filtro" Parametros de filtro y paginacion
     * @return PaginaResponse con la lista de guias y metadatos de paginacion
     */
    @Transactional(readOnly = true)
    public PaginaResponse<GuiaResponse> listarHistorial(FiltroGuiaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPagina(), filtro.getTamanio());
        Page<GuiaRemision> pagina = guiaRepo.buscarConFiltros(
                filtro.getFechaDesde(),
                filtro.getFechaHasta(),
                filtro.getEstado(),
                filtro.getOrdenId(),
                pageable
        );
        return PaginaResponse.<GuiaResponse>builder()
                .contenido(pagina.getContent().stream().map(GuiaMapper::toResponse).toList())
                .paginaActual(pagina.getNumber())
                .tamanio(pagina.getSize())
                .totalElementos(pagina.getTotalElements())
                .totalPaginas(pagina.getTotalPages())
                .ultima(pagina.isLast())
                .build();
    }

    // RF3.5: Descargar PDF de la guia
    /**
     * genera el pdf de una guia en estado ACEPTADA.
     * Solo las guias aceptadas por SUNAT pueden descargarse como pdf oficial.
     *
     * PATRÓN ACL: se delega al DocumentoPdfPort — GuiaService no conoce iTextPDF.
     *
     * @param "uuid" UUID de la guia
     * @return Bytes del archivo pdf generado
     * @throws "GuiaException" si la guia no esta en estado ACEPTADA
     */
    @Transactional(readOnly = true)
    public byte[] descargarPdf(String uuid) {
        GuiaRemision guia = obtenerEntidad(uuid);
        if (guia.getEstado() != EstadoGuia.ACEPTADA) {
            throw GuiaException.estadoInvalido(guia.getEstado().name(), "descargar pdf");
        }
        return documentoPdfPort.generar(guia);
    }

    /**
     * retorna el nombre del archivo PDF segun el formato SUNAT: GRE-{serie}-{correlativo}.pdf
     */
    @Transactional(readOnly = true)
    public String nombrePdf(String uuid) {
        return documentoPdfPort.nombreArchivo(obtenerEntidad(uuid));
    }

    // Reenvio automatico
    /**
     * procesa la cola de guias pendientes de reenvio.
     * llamado por ReenvioScheduler cada 5 minutos.
     * reintenta el envio de guias cuyo proximo_reenvio ya vencio.
     */
    @Transactional
    public void procesarColaReenvio() {
        List<GuiaRemision> pendientes = guiaRepo.findPendientesParaReenvio(LocalDateTime.now());
        log.info("cola de reenvio: {} guía(s) pendientes", pendientes.size());
        pendientes.forEach(g -> enviarASunat(g.getUuid(), "SISTEMA"));
    }

    // ── Validaciones contra sistemas externos (best-effort via ACL) ─────────

    /**
     * Valida un RUC contra el padrón de SUNAT vía el IdentidadValidadorPort (ACL).
     * Si el port falla, solo registra un warning y continua.
     * El dominio no conoce APIsPerú — solo conoce el port y el InfoRuc del dominio.
     *
     * @param ruc  Numero de RUC a validar
     * @param tipo "remitente" o "destinatario" para identificar en el log
     */
    private void validarRuc(String ruc, String tipo) {
        try {
            Optional<InfoRuc> info = identidadPort.validarRuc(ruc);
            info.ifPresentOrElse(
                    i -> log.info("RUC {} ({}) validado: {} — {}", ruc, tipo, i.razonSocial(), i.estado()),
                    () -> log.warn("RUC {} ({}) no encontrado en SUNAT", ruc, tipo)
            );
        } catch (RuntimeException ex) {
            // No bloquear la creacion si el port de validacion no responde
            log.warn("no se pudo validar ruc {} ({}): {}", ruc, tipo, ex.getMessage());
        }
    }

    /**
     * Valida el DNI del conductor vía el IdentidadValidadorPort (ACL).
     * Si el port responde, actualiza el nombre en el request con el nombre oficial de RENIEC.
     * Si falla, mantiene el nombre ingresado manualmente.
     *
     * @param dni     Numero de DNI del conductor
     * @param request Request de creacion donde se actualiza el nombre si corresponde
     */
    private void validarDni(String dni, CrearGuiaRequest request) {
        try {
            Optional<InfoDni> info = identidadPort.validarDni(dni);
            info.ifPresentOrElse(
                    i -> {
                        log.info("DNI {} del conductor validado: {}", dni, i.nombreCompleto());
                        request.setConductorNombre(i.nombreCompleto());
                    },
                    () -> log.warn("DNI {} del conductor no encontrado en RENIEC", dni)
            );
        } catch (RuntimeException ex) {
            log.warn("No se pudo validar DNI del conductor {}: {}", dni, ex.getMessage());
        }
    }

    // ── Métodos auxiliares internos ──────────────────────────────────────────

    /**
     * busca una guia por UUID y lanza GuiaException si no existe.
     * centraliza el manejo de "not found" para todos los metodos publicos.
     */
    private GuiaRemision obtenerEntidad(String uuid) {
        return guiaRepo.findByUuid(uuid)
                .orElseThrow(() -> GuiaException.noEncontrada(uuid));
    }

    /**
     * persiste un registro en la tabla de auditoria.
     * Se llama en todas las operaciones sensibles: crear, enviar, anular.
     *
     * @param "guiaId"   ID interno de la guia afectada
     * @param "accion"   Codigo de la accion ejecutada
     * @param "resultado" Resultado de la accion
     * @param "detalle"  Descripcion adicional del resultado
     * @param "usuario"  Usuario que ejecuto la accion
     * @param "ip"       Direccion IP del cliente
     */
    private void registrarAuditoria(Long guiaId, String accion,
                                    AuditoriaGuia.Resultado resultado,
                                    String detalle, String usuario, String ip) {
        auditoriaRepo.save(AuditoriaGuia.builder()
                .guiaId(guiaId)
                .accion(accion)
                .resultado(resultado)
                .detalle(detalle)
                .usuario(usuario)
                .ipOrigen(ip)
                .build());
    }
}
