package com.tms.logistica.guideservice.service;

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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GuiaService - Logica de negocio del Modulo 3
 *
 * este servicio coordina el ciclo de vida completo de una Guia de Remision
 *  creacion, validacion de datos contra APIs externas, envio a
 * sunat via el ose de apisperu, consulta de estado, anulacion y descarga de
 * PDF.
 *
 * dependencias externas:
 *   - ApisPeruService: consulta ruc/dni a dniruc.apisperu.com
 *   - SunatGateway: envia la guia a facturacion.apisperu.com
 *   - GREBuilder: construye el xml segun el esquema XSD de sunat
 *   - PdfGuiaService: genera el pdf de la guia aceptada
 *
 * Estrategia de resiliencia:
 *   Las validaciones de ruc y dni son de mejor esfuerzo: si la api externa
 *   falla, se registra un warning y se continua con los datos manuales.
 *   El envio a sunat tambien tiene manejo de errores: si falla la conexion,
 *   la guia queda en estado PENDIENTE y el scheduler la reintenta cada 5 minutos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuiaService {
    // repositorio principal de guias de remision
    private final GuiaRemisionRepository guiaRepo;
    // repositorio para registrar todos los eventos de auditoria del modulo
    private final AuditoriaGuiaRepository auditoriaRepo;
    // genera el siguiente numero correlativo de forma sincronizada
    private final NumeroGuiaGenerator     numeroGenerator;
    // adaptador para enviar la guia al ose de apisperu
    private final SunatGateway            sunatGateway;
    // constructor del xml de la gre segun el esquema xsd oficial de sunat
    private final GREBuilder              greBuilder;
    // adaptador para validar ruc y dni contra dniruc.apisperu.com
    private final ApisPeruService         apisPeruService;
    // generador de pdf con itext html2pdf para guias aceptadas
    private final PdfGuiaService          pdfGuiaService;

    // RF3.1: Crear guia de remision electronica
    /**
     * crea una nueva Guia de Remision Electronica en estado pendiente.
     *
     * Proceso:
     *  1. valida ruc del remitente y destinatario contra apisperu
     *  2. valida dni del conductor y actualiza el nombre oficial si la api
     *  responde
     *  3. genera el numero de serie y correlativo siguiente
     *  4. persiste la guia con todos sus detalles en mysql
     *  5. registra el evento de creacion en la tabla de auditoria
     *
     * @param "request" datos del formulario de la guia son validados con @Valid
     * en el controller
     * @param "usuario" identificador del usuario que ejecuta la accion
     * @return GuiaResponse con los datos de la guia creada y su uuid
     */
    @Transactional
    public GuiaResponse crearGuia(CrearGuiaRequest request, String usuario) {
        log.info("Creando guía para orden {} por usuario {}", request.getOrdenId(), usuario);
        // validaciones contra apis externas
        validarRuc(request.getRemitenteRuc(), "remitente");
        // genera serie y correlativo: siempre usa la serie T001 para guias
        // de traslado
        validarRuc(request.getDestinatarioRuc(), "destinatario");
        validarDni(request.getConductorDni(), request);

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
                "guia " + guardada.getSerie() + "-" + guardada.getCorrelativo() + " creada", usuario, null);

        return GuiaMapper.toResponse(guardada);
    }
    // RF3.3: Enviar guia a SUNAT

    /**
     * envia la guia al ose y actualiza su estado segun la respuesta
     *
     * solo se puede enviar una guia en estado pendiente
     * mientras dura la llamada al ose el estado cambia a EN_PROCESO
     * al terminar el estado queda ACEPTADA o RECHAZADA
     *
     * Si hay un error de red, la guia vuelve a PENDIENTE con un proximo_reenvio
     * programado para 5 minutos despues. El scheduler la reintentara automaticamente.
     *
     * @param "uuid" UUID unico de la guia a enviar
     * @param "usuario" Usuario que ejecuta la accion
     * @return GuiaResponse con el estado final y el cdr de sunat
     */

    @Transactional
    public GuiaResponse enviarASunat(String uuid, String usuario) {
        GuiaRemision guia = obtenerEntidad(uuid);
        // solo se puede enviar una guia que este en estado PENDIENTE
        if (guia.getEstado() != EstadoGuia.PENDIENTE) {
            throw GuiaException.estadoInvalido(guia.getEstado().name(),
                    "enviar a sunat");
        }
        // marca EN_PROCESO para indicar que el envio esta en curso
        guia.setEstado(EstadoGuia.EN_PROCESO);

        try {
            // llama al OSE con todos los datos de la guia
            SunatGateway.CdrResult cdr = sunatGateway.enviar(
                    guia.getSerie(), guia.getCorrelativo(),
                    guia.getMotivoTraslado(), guia.getModalidad(),
                    guia.getFechaInicio().toString(),
                    guia.getRemitenteRazon(), guia.getRemitenteDir(), guia.getRemitenteUbigeo(),
                    guia.getDestinatarioRuc(), guia.getDestinatarioRazon(),
                    guia.getDestinatarioDir(), guia.getDestinatarioUbigeo(),
                    guia.getDestinoDir(), guia.getDestinoUbigeo(),
                    guia.getVehiculoPlaca(),
                    guia.getConductorDni(), guia.getConductorNombre(), guia.getConductorLicencia(),
                    // convierte los detalles de la entidad al dto que espera
                    // SunatGateway
                    guia.getDetalles().stream()
                            .map(d -> new SunatGateway.DetalleGuia(
                                    d.getDescripcion(), d.getUnidadMedida(),
                                    d.getCantidad(), d.getPesoBrutoKg()))
                            .toList()
            );
            // guarda el cdr completo para auditoria y trazabilidad
            guia.setCdrCodigo(cdr.codigo());
            guia.setCdrDescripcion(cdr.descripcion());
            guia.setCdrResponse(cdr.rawResponse());
            // codigo "0" = sunat acepto la guia; cualquier otro codigo =
            // rechazada
            guia.setEstado("0".equals(cdr.codigo()) ? EstadoGuia.ACEPTADA : EstadoGuia.RECHAZADA);
            guia.setIntentosEnvio(guia.getIntentosEnvio() + 1);

            registrarAuditoria(guia.getId(), "ENVIAR_SUNAT", AuditoriaGuia.Resultado.OK,
                    "CDR código: " + cdr.codigo(), usuario, null);

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
     * anula una guia de remision registrando el motivo y el usuario
     * responsable.
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
     * retorna el estado actual de una guia por su UUID
     * consulta el estado guardado en la bd local
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
     * lista todas las guias asociadas a una orden de transporte
     * una orden puede tener multiples guias
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
     * los resultados se ordenan por fecha de creacion descendente
     * @param "filtro" Parametros de filtro y paginacion
     * @return PaginaResponse con la lista de guias y metadatos de paginacion
     */
    @Transactional(readOnly = true)
    public PaginaResponse<GuiaResponse> listarHistorial(FiltroGuiaRequest filtro) {
        // pagerequest encapsula el numero de pagina y el tamano del lote
        PageRequest pageable = PageRequest.of(filtro.getPagina(), filtro.getTamanio());
        // la query en el repositorio permite que cada filtro sea opcional
        Page<GuiaRemision> pagina = guiaRepo.buscarConFiltros(
                filtro.getFechaDesde(),
                filtro.getFechaHasta(),
                filtro.getEstado(),
                filtro.getOrdenId(),
                pageable
        );
        // mapea la pagina de entidades al dto de respuesta con metadatos de
        // paginacion
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
     * genera el pdf de una guia en estado aceptada
     * Solo las guias aceptadas por sunat pueden descargarse como pdf oficial
     * @param "uuid" UUID de la guia
     * @return Bytes del archivo pdf generado con itext
     * @throws "GuiaException" si la guia no esta en estado aceptada
     */
    @Transactional(readOnly = true)
    public byte[] descargarPdf(String uuid) {
        GuiaRemision guia = obtenerEntidad(uuid);
        if (guia.getEstado() != EstadoGuia.ACEPTADA) {
            throw GuiaException.estadoInvalido(guia.getEstado().name(),
                    "descargar pdf");
        }
        return pdfGuiaService.generarPdf(guia);
    }
    /**
     * retorna el nombre del archivo PDF segun el formato sunat: GRE-{serie}-
     * {correlativo}.pdf
     */
    @Transactional(readOnly = true)
    public String nombrePdf(String uuid) {
        return pdfGuiaService.nombreArchivo(obtenerEntidad(uuid));
    }

    // Reenvio automatico
    /**
     * procesa la cola de guias pendientes de reenvio
     * llamado por ReenvioScheduler cada 5 minutos
     * reintenta el envio de guias cuyo proximo_reenvio ya vencio
     */
    @Transactional
    public void procesarColaReenvio() {
        List<GuiaRemision> pendientes = guiaRepo.findPendientesParaReenvio(LocalDateTime.now());
        log.info("cola de reenvio: {} guía(s) pendientes", pendientes.size());
        pendientes.forEach(g -> enviarASunat(g.getUuid(), "SISTEMA"));
    }

    // validaciones contra apis externas
    // estas validaciones son de mejor esfuerzo: si la api falla, se registra
    // un warning en el log pero
    // no se lanza excepcion. La guia se crea igualmente con los datos manuales.

    /**
     * Valida un RUC contra el padron de sunat via apisperu.
     * Si la api falla, solo registra un warning y continua.
     *
     * @param "ruc" Numero de ruc a validar
     * @param "tipo" "remitente" o "destinatario" para identificar en el log
     */
    private void validarRuc(String ruc, String tipo) {
        try {
            ApisPeruService.RucInfo info = apisPeruService.consultarRuc(ruc);
            log.info("ruc {} ({}) validado: {} - {}", ruc, tipo,
                    info.razonSocial(), info.estado());
        } catch (RuntimeException ex) {
            // No bloquear la creacion si la API de validacion no responde
            log.warn("no se pudo validar ruc {} ({}): {}", ruc, tipo,
                    ex.getMessage());
        }
    }
    /**
     * valida el dni del conductor contra renic via apisperu.
     * si la api responde, actualiza el nombre en el request con el nombre
     * oficial.
     * si la api falla, mantiene el nombre ingresado manualmente.
     * @param "dni" Numero de DNI del conductor
     * @param "request" Request de creacion donde se actualiza el nombre si corresponde
     */
    private void validarDni(String dni, CrearGuiaRequest request) {
        try {
            ApisPeruService.DniInfo info = apisPeruService.consultarDni(dni);
            log.info("dni {} del conductor validado: {}", dni,
                    info.nombreCompleto());
            request.setConductorNombre(info.nombreCompleto());
        } catch (RuntimeException ex) {
            log.warn("No se pudo validar dni del conductor {}: {}", dni,
                    ex.getMessage());
        }
    }
    // Metodos auxiliares internos
    /**
     * busca una guia por UUID y lanza GuiaException si no existe.
     * centraliza el manejo de "not found" para todos los metodos publicos.
     */private GuiaRemision obtenerEntidad(String uuid) {
        return guiaRepo.findByUuid(uuid)
                .orElseThrow(() -> GuiaException.noEncontrada(uuid));
    }
    /**
     *persiste un registro en la tabla de auditoria.
     * Se llama en todas las operaciones sensibles: crear, enviar, anular.
     * @param "guiaId" ID interno de la guia afectada
     * @param "accion" Codigo de la accion ejecutada
     * @param "resultado" Resultado de la accion
     * @param "detalle" Descripcion adicional del resultado
     * @param "usuario" Usuario que ejecuto la accion
     * @param "ip" Direccion IP del cliente
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
