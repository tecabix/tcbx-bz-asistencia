package com.tecabix.bz.asistencia;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Asistencia;
import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.AsistenciaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB073;
import com.tecabix.sv.rq.RQSV082;

public class Asistencia003BZ {

    private final AsistenciaRepository asistenciaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final Catalogo enComida;

    public Asistencia003BZ(
            final AsistenciaRepository asistenciaRepository,
            final TrabajadorRepository trabajadorRepository,
            final Catalogo enComida) {
        super();
        this.asistenciaRepository = asistenciaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.enComida = enComida;
    }

    public ResponseEntity<RSB073> registrarSalidaComida(final RQSV082 rqsv082) {

        RSB073 response = rqsv082.getRsb073();

        Sesion sesion = rqsv082.getSesion();

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

        if (asistencia.getComidaSalida() != null) {
            return response.conflict("La salida a comida ya fue registrada.");
        }

        LocalDateTime fechaActual = LocalDateTime.now();

        asistencia.setComidaSalida(fechaActual);
        asistencia.setIdUsuarioModificado(sesion.getUsuario().getId());
        asistencia.setFechaModificado(fechaActual);
        asistencia.setEstatus(enComida);

        asistenciaRepository.save(asistencia);

        return response.ok(asistencia);
    }
}