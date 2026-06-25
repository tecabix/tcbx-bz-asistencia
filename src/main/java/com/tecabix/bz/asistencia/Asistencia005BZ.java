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
import com.tecabix.res.b.RSB075;
import com.tecabix.sv.rq.RQSV084;

public class Asistencia005BZ {

    private final AsistenciaRepository asistenciaRepository;

    private final TrabajadorRepository trabajadorRepository;

    private final Catalogo finalizado;

    public Asistencia005BZ(
            final AsistenciaRepository asistenciaRepository,
            final TrabajadorRepository trabajadorRepository,
            final Catalogo finalizado) {
        super();
        this.asistenciaRepository = asistenciaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.finalizado = finalizado;
    }

    public ResponseEntity<RSB075> registrarSalida(final RQSV084 rqsv084) {

        RSB075 response = rqsv084.getRsb075();

        Sesion sesion = rqsv084.getSesion();

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

        if (asistencia.getSalida() != null) {
            return response.conflict("La salida ya fue registrada.");
        }

        LocalDateTime fechaActual = LocalDateTime.now();

        asistencia.setSalida(fechaActual);

        asistencia.setDuracion(
                (int) ChronoUnit.SECONDS.between(
                        asistencia.getEntrada(),
                        fechaActual));

        asistencia.setIdUsuarioModificado(sesion.getUsuario().getId());

        asistencia.setFechaModificado(fechaActual);

        asistencia.setEstatus(finalizado);

        asistenciaRepository.save(asistencia);

        return response.ok(asistencia);
    }
}