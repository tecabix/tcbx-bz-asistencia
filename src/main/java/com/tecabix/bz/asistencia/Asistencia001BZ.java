package com.tecabix.bz.asistencia;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Asistencia;
import com.tecabix.db.entity.Catalogo;
import com.tecabix.db.entity.Sesion;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.AsistenciaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB052;
import com.tecabix.sv.rq.RQSV060;

public class Asistencia001BZ {

	private final AsistenciaRepository asistenciaRepository;
	private final TrabajadorRepository trabajadorRepository;
	private final Catalogo enProceso;

	public Asistencia001BZ(
			final AsistenciaRepository asistenciaRepository,
			final TrabajadorRepository trabajadorRepository,
			final Catalogo enProceso) {
		super();
		this.asistenciaRepository = asistenciaRepository;
		this.trabajadorRepository = trabajadorRepository;
		this.enProceso = enProceso;
	}

	public ResponseEntity<RSB052> registrarEntrada(final RQSV060 rqsv060) {

		RSB052 response = rqsv060.getRsb052();
		Sesion sesion = rqsv060.getSesion();

		Trabajador trabajador = trabajadorRepository
				.findByClaveUsuario(sesion.getUsuario().getClave())
				.orElse(null);

		if(trabajador == null) {
			return response.notFound("No se encontró el trabajador.");
		}

		if(asistenciaRepository
				.findByTrabajadorPendiente(trabajador.getId())
				.isPresent()) {
			return response.conflict("El empleado ya tiene una entrada registrada en proceso.");
		}

		LocalDateTime fechaActual = LocalDateTime.now();

		Asistencia asistencia = new Asistencia();
		asistencia.setTrabajador(trabajador);
		asistencia.setEntrada(fechaActual);
		asistencia.setUsuarioCreador(sesion.getUsuario());
		asistencia.setFechaCreacion(fechaActual);
		asistencia.setIdUsuarioModificado(sesion.getUsuario().getId());
		asistencia.setFechaModificado(fechaActual);
		asistencia.setEstatus(enProceso);
		asistencia.setClave(UUID.randomUUID());

		asistenciaRepository.save(asistencia);

		return response.ok(asistencia);
	}

}