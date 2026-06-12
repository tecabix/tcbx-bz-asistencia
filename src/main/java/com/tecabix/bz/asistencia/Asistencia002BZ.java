package com.tecabix.bz.asistencia;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Asistencia;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.AsistenciaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB053;
import com.tecabix.sv.rq.RQSV061;

public class Asistencia002BZ {

    private final AsistenciaRepository asistenciaRepository;
    private final TrabajadorRepository trabajadorRepository;

    public Asistencia002BZ(
            final AsistenciaRepository asistenciaRepository,
            final TrabajadorRepository trabajadorRepository) {
        super();
        this.asistenciaRepository = asistenciaRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    public ResponseEntity<RSB053> pendiente(final RQSV061 rqsvx) {

        RSB053 response = rqsvx.getRsb053();
        Sesion sesion = rqsvx.getSesion();

        Trabajador trabajador = trabajadorRepository
                .findByClaveUsuario(sesion.getUsuario().getClave())
                .orElse(null);

        if (trabajador == null) {
            return response.notFound("No se encontro el trabajador");
        }

        Asistencia asistencia = asistenciaRepository
                .findByTrabajadorPendiente(trabajador.getId())
                .orElse(null);

        if (asistencia == null) {
            return response.notFound("No existe asistencia pendiente");
        }

        return response.ok(asistencia);
    }

}