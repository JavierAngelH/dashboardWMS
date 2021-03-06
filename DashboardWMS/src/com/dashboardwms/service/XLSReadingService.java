package com.dashboardwms.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.dashboardwms.exceptions.RutaInvalidaException;

public interface XLSReadingService {

	public LinkedHashMap<Date, Double> getStreamingMinutes(String nombreAplicacion, Date fechaInicio, Date fechaFin);
	public LinkedHashMap<Date, Integer> getStreamingSessions(String nombreAplicacion, Date fechaInicio, Date fechaFin);
	public LinkedHashMap<Date, Integer> getCustomRegistrations(String nombreAplicacion, Date fechaInicio, Date fechaFin);
	public Boolean verificarAppMovil(String nombreAplicacion);
	public List<String> getListaAplicacionesMoviles() throws RutaInvalidaException;
	public LinkedHashMap<String, Integer> getRegistrationsByDevice(String nombreAplicacion);
}
