package com.tecabix.bz.asistencia;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Asistencia;
import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.AsistenciaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB074;
import com.tecabix.sv.rq.RQSV083;

public class Asistencia004BZ {

    private final AsistenciaRepository asistenciaRepository;

    private final TrabajadorRepository trabajadorRepository;

    private final Catalogo enProceso;

    public Asistencia004BZ(
            final AsistenciaRepository asistenciaRepository,
            final TrabajadorRepository trabajadorRepository,
            final Catalogo enProceso) {
        super();
        this.asistenciaRepository = asistenciaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.enProceso = enProceso;
    }

    public ResponseEntity<RSB074> registrarRegresoComida(final RQSV083 rqsv083) {

        RSB074 response = rqsv083.getRsb074();

        Sesion sesion = rqsv083.getSesion();

        Trabajador trabajador = trabajadorRepository
                .findByClaveUsuario(sesion.getUsuario().getClave())
                .orElse(null);

        if (trabajador == null) {
            return response.notFound("No se encontró el trabajador.");
        }

        Asistencia asistencia = asistenciaRepository
                .findByTrabajadorPendiente(trabajador.getId())
                .orElse(null);

        if (asistencia == null) {
            return response.notFound("No existe asistencia pendiente.");
        }

        if (asistencia.getComidaSalida() == null) {
            return response.conflict("No existe salida a comida registrada.");
        }

        if (asistencia.getComidaRegreso() != null) {
            return response.conflict("El regreso de comida ya fue registrado.");
        }

        LocalDateTime fechaActual = LocalDateTime.now();

        asistencia.setComidaRegreso(fechaActual);

        asistencia.setDuracionComida(
                (int) ChronoUnit.SECONDS.between(
                        asistencia.getComidaSalida(),
                        fechaActual));

        asistencia.setIdUsuarioModificado(sesion.getUsuario().getId());

        asistencia.setFechaModificado(fechaActual);

        asistencia.setEstatus(enProceso);

        asistenciaRepository.save(asistencia);

        return response.ok(asistencia);
    }
}