package com.tecabix.bz.asistencia;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.tecabix.db.entity.Asistencia;
import com.tecabix.db.entity.Trabajador;
import com.tecabix.db.repository.AsistenciaRepository;
import com.tecabix.db.repository.TrabajadorRepository;
import com.tecabix.res.b.RSB076;
import com.tecabix.sv.rq.RQSV085;

public class Asistencia006BZ {

    private final AsistenciaRepository asistenciaRepository;

    private final TrabajadorRepository trabajadorRepository;

    public Asistencia006BZ(
            final AsistenciaRepository asistenciaRepository,
            final TrabajadorRepository trabajadorRepository) {
        super();
        this.asistenciaRepository = asistenciaRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    public ResponseEntity<RSB076> consultar(final RQSV085 rqsv085) {

        RSB076 response = rqsv085.getRsb076();
        List<Long> trabajadores = null;
        if(rqsv085.getTrabajadores() != null) {

            trabajadores = rqsv085
                    .getTrabajadores()
                    .stream()
                    .map(x -> trabajadorRepository.findByClave(x).orElse(null))
                    .filter(x -> x != null)
                    .map(Trabajador::getId)
                    .collect(Collectors.toList());

        }else {
        	Trabajador trabajador = trabajadorRepository.findByClaveUsuario(rqsv085.getSesion().getUsuario().getClave()).orElse(null);
        	if (trabajador == null) {
                return response.notFound("No existen trabajador de la sesion.");
            }
        	trabajadores = Stream.of(trabajador).map(Trabajador::getId).collect(Collectors.toList());
        }
        if (trabajadores.isEmpty()) {
            return response.notFound("No existen trabajadores.");
        }

        Pageable pageable = PageRequest.of(
                rqsv085.getPagina(),
                rqsv085.getSize());

        Page<Asistencia> asistencias = asistenciaRepository
                .findByTrabajadores(
                        trabajadores,
                        rqsv085.getInicio(),
                        rqsv085.getFin(),
                        pageable);

        return response.ok(asistencias);
    }
}