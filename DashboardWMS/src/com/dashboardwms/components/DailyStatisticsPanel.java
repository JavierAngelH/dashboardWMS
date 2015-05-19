package com.dashboardwms.components;


import java.awt.Font;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.vaadin.addon.JFreeChartWrapper;

import com.dashboardwms.service.AplicacionService;
import com.dashboardwms.service.ClienteService;
import com.dashboardwms.utilities.Utilidades;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DailyStatisticsPanel extends Panel {

	private CssLayout dashboardPanels;
	private final VerticalLayout root;
	public PopupDateField dfFecha = new PopupDateField();
	private CssLayout componentGraficoPeriodo = new CssLayout();
	private CssLayout componentGraficoDispositivos = new CssLayout();
	private AplicacionService aplicacionService;
	private ClienteService clienteService;
	private Date today = new Date();
	public ComboBox cboxPeriodo = new ComboBox();
	private final Table tablaInformacionPeriodo = new Table();
	private final Table tablaDispositivosPeriodo = new Table();
	private Label captionTablaPeriodo = new Label();
	private Label captionTablaDispositivos = new Label();
	private Label captionInfoPeriodo = new Label();
	private Label captionInfoDispositivos = new Label();
	private String emisora;
	private static Boolean tipoRango = true;
	Calendar calendar = Calendar.getInstance();

	public void setEmisora(String emisora) {
		this.emisora = emisora;
	}

	public DailyStatisticsPanel() {
		cboxPeriodo.setImmediate(true);
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -30);

		addStyleName(ValoTheme.PANEL_BORDERLESS);
		setSizeFull();
		root = new VerticalLayout();
		root.setSizeFull();
		root.setMargin(true);
		root.addStyleName("dashboard-view");
		setContent(root);
		Responsive.makeResponsive(root);

		Responsive.makeResponsive(tablaInformacionPeriodo);
		Responsive.makeResponsive(tablaDispositivosPeriodo);
		Responsive.makeResponsive(componentGraficoPeriodo);
		Responsive.makeResponsive(componentGraficoDispositivos);
		Responsive.makeResponsive(cboxPeriodo);
		Responsive.makeResponsive(dfFecha);

		root.addComponent(buildHeader());

		Component content = buildContent();
		
		root.addComponent(content);
		root.setExpandRatio(content, 1);

	}

	public void setAplicacionService(AplicacionService aplicacionService) {
		this.aplicacionService = aplicacionService;
	}

	public void setClienteService(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	private Component buildHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.addStyleName("viewheader");
		header.setSpacing(true);
		Responsive.makeResponsive(header);
		buildFilter();
		Label title = new Label("Oyentes Diarios");
		title.setSizeUndefined();
		title.addStyleName(ValoTheme.LABEL_H1);
		title.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		header.addComponent(title);
	     Label lbVer = new Label("Ver: ");
	        HorizontalLayout tools = new HorizontalLayout(lbVer, cboxPeriodo, dfFecha);
	        tools.setComponentAlignment(lbVer, Alignment.MIDDLE_CENTER);
		dfFecha.setVisible(false);
		Responsive.makeResponsive(tools);
		tools.setSpacing(true);
		tools.addStyleName("toolbar");
		header.addComponent(tools);

		return header;
	}

	private void buildFilter() {
		dfFecha.setValue(today);
		dfFecha.setDateFormat("dd-MM-yyyy");
		dfFecha.setImmediate(true);
		dfFecha.setRangeEnd(today);
		dfFecha.setRangeStart(calendar.getTime());

		dfFecha.addValueChangeListener(dfChangeListener);
		dfFecha.setTextFieldEnabled(false);

		cboxPeriodo.setImmediate(true);
		cboxPeriodo.setNullSelectionAllowed(false);
		cboxPeriodo.setInvalidAllowed(false);
		BeanItemContainer<String> listaPeriodosContainer = new BeanItemContainer<String>(
				String.class);

		listaPeriodosContainer.addAll(Utilidades.LISTA_PERIODOS);
		cboxPeriodo.setContainerDataSource(listaPeriodosContainer);
		cboxPeriodo.setTextInputAllowed(false);
		cboxPeriodo.select(Utilidades.LISTA_PERIODOS.get(0));
		cboxPeriodo.addValueChangeListener(cboxChangeListener);
	}

	private Component buildContent() {
		dashboardPanels = new CssLayout();
		dashboardPanels.addStyleName("dashboard-panels");
		Responsive.makeResponsive(dashboardPanels);

		dashboardPanels.addComponent(componentGraficoPeriodo);
	
		Component componentTablaPeriodo = buildComponentTablaInfoPeriodo();
		dashboardPanels.addComponent(componentTablaPeriodo);
	
		dashboardPanels.addComponent(componentGraficoDispositivos);
		
		Component componentTablaDispositivos = buildComponentTablaDispositivos();
		dashboardPanels.addComponent(componentTablaDispositivos);
		String captionPeriodo = "Cantidad de Oyentes " + Utilidades.LISTA_PERIODOS.get(0);
		String captionDispositivos = "Tipos de Conexión " + Utilidades.LISTA_PERIODOS.get(0);
		captionTablaPeriodo.setValue(captionPeriodo);
		captionTablaDispositivos.setValue(captionDispositivos);
		captionInfoPeriodo.setValue(captionPeriodo);
		captionInfoDispositivos.setValue(captionDispositivos);

		return dashboardPanels;
	}

	private Component buildComponentTablaInfoPeriodo() {
		buildTableInfoPeriodo();
		Component contentWrapper = createContentWrapperPeriodo(tablaInformacionPeriodo);

		contentWrapper.addStyleName("top10-revenue");
		Responsive.makeResponsive(contentWrapper);
		return contentWrapper;
	}

	private Component buildComponentTablaDispositivos(){
		buildTableDispositivos();
		Component contentWrapper = createContentWrapperDispositivos(tablaDispositivosPeriodo);
		contentWrapper.addStyleName("top10-revenue");
		Responsive.makeResponsive(contentWrapper);
		return contentWrapper;
	}
	
	private void buildTableDispositivos(){
		tablaDispositivosPeriodo.setSizeFull();
		tablaDispositivosPeriodo.addStyleName(ValoTheme.TABLE_BORDERLESS);
		tablaDispositivosPeriodo.addStyleName(ValoTheme.TABLE_NO_STRIPES);
		tablaDispositivosPeriodo.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
		tablaDispositivosPeriodo.addStyleName(ValoTheme.TABLE_SMALL);
		tablaDispositivosPeriodo.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		tablaDispositivosPeriodo.addContainerProperty("Key", String.class, null);
		tablaDispositivosPeriodo.addContainerProperty("Value", Integer.class,
				null);
		tablaDispositivosPeriodo.setColumnAlignment("Value", Align.RIGHT);
	}
		
	private void buildTableInfoPeriodo() {

		tablaInformacionPeriodo.setSizeFull();
		tablaInformacionPeriodo.addStyleName(ValoTheme.TABLE_BORDERLESS);
		tablaInformacionPeriodo.addStyleName(ValoTheme.TABLE_NO_STRIPES);
		tablaInformacionPeriodo.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
		tablaInformacionPeriodo.addStyleName(ValoTheme.TABLE_SMALL);
		tablaInformacionPeriodo.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		tablaInformacionPeriodo.addContainerProperty("Key", String.class, null);
		tablaInformacionPeriodo.addContainerProperty("Value", Double.class,
				null);
		tablaInformacionPeriodo.setColumnAlignment("Value", Align.RIGHT);
	}
	
	private void fillContentWrapperGraficoDispositivos(Date fechaInicio, Date fechaFin)
			throws InvalidResultSetAccessException, ParseException {
		LinkedHashMap<String, Integer> lista = new LinkedHashMap<>();

	
		
		
		lista = clienteService.getCantidadDispositivosRangoFechas(emisora, fechaInicio, fechaFin);
		Color[] colors = {SolidColor.GREEN, SolidColor.MAGENTA, SolidColor.CYAN};
		 Chart chart = new Chart(ChartType.COLUMN);
		 chart.setHeight("100%");
		 chart.setWidth("100%");
	        Configuration conf = chart.getConfiguration();
	        conf.getxAxis().setType(AxisType.CATEGORY);

	        conf.setTitle("");

	        XAxis xAxis = new XAxis();
	        DataSeries ls = new DataSeries();
	        ls.setName("Tipo de Conexión");
			Iterator it = lista.entrySet().iterator();
			int i = 0;
				while (it.hasNext()) {

					Map.Entry pairs = (Map.Entry) it.next();
					 DataSeriesItem item = new DataSeriesItem((String) pairs.getKey(),
							  (Integer) pairs.getValue());
				item.setColor(colors[i]);
			            ls.add(item);
					it.remove();
					i++;
				}
	        conf.addSeries(ls);
	        
	        conf.addxAxis(xAxis);

	        YAxis yAxis = new YAxis();
	        yAxis.setMin(0);
	        yAxis.setTitle(new Title(""));
	        conf.addyAxis(yAxis);

	     

	        Tooltip tooltip = new Tooltip();
	        tooltip.setFormatter("''+ this.y +'	'+'Conexiones'");
	        conf.setTooltip(tooltip);

	      

	        chart.drawChart(conf);

		Responsive.makeResponsive(chart);
		chart.setSizeFull();
		componentGraficoDispositivos.removeAllComponents();
		componentGraficoDispositivos.setWidth("100%");
		componentGraficoDispositivos.addStyleName("dashboard-panel-slot");

		CssLayout card = new CssLayout();
		card.setWidth("100%");
		card.addStyleName(ValoTheme.LAYOUT_CARD);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("dashboard-panel-toolbar");
		toolbar.setWidth("100%");

		captionInfoDispositivos.addStyleName(ValoTheme.LABEL_H4);
		captionInfoDispositivos.addStyleName(ValoTheme.LABEL_COLORED);
		captionInfoDispositivos.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		chart.setCaption(null);

		MenuBar tools = new MenuBar();
		tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

		MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

			@Override
			public void menuSelected(final MenuItem selectedItem) {
				if (!componentGraficoDispositivos.getStyleName().contains("max")) {
					selectedItem.setIcon(FontAwesome.COMPRESS);
					toggleMaximized(componentGraficoDispositivos, true);
				} else {
					componentGraficoDispositivos.removeStyleName("max");
					selectedItem.setIcon(FontAwesome.EXPAND);
					toggleMaximized(componentGraficoDispositivos, false);
				}
			}
		});
		max.setStyleName("icon-only");

		toolbar.addComponents(captionInfoDispositivos, tools);
		toolbar.setExpandRatio(captionInfoDispositivos, 1);
		toolbar.setComponentAlignment(captionInfoDispositivos, Alignment.MIDDLE_LEFT);

		card.addComponents(toolbar, chart);
		componentGraficoDispositivos.addComponent(card);
	}
		
	private void fillContentWrapperGraficoPeriodo(Date fechaInicio, Date fechaFin)
			throws InvalidResultSetAccessException, ParseException {
		LinkedHashMap<Date, Integer> listaAplicaciones = new LinkedHashMap<>();

		listaAplicaciones = aplicacionService.getUsuariosConectadosPorHora(
				emisora, fechaInicio, fechaFin);
 Chart vaadinChartPeriodo = new Chart();
		vaadinChartPeriodo.setHeight("100%");
		vaadinChartPeriodo.setWidth("100%");
        Configuration configuration = vaadinChartPeriodo.getConfiguration();
        configuration.getChart().setType(ChartType.SPLINE);



        configuration.getxAxis().setType(AxisType.DATETIME);
       
        				
        Axis yAxis = configuration.getyAxis();
        yAxis.setTitle(new Title(""));
        yAxis.setMin(0);

        
        configuration.setTitle("");
        configuration.getTooltip().setxDateFormat("%d-%m-%Y %H:%M");
        DataSeries ls = new DataSeries();
        ls.setPlotOptions(new PlotOptionsSpline());
        ls.setName("Oyentes Conectados");
        
		Iterator it = listaAplicaciones.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pairs = (Map.Entry) it.next();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date) pairs.getKey());
			  DataSeriesItem item = new DataSeriesItem(calendar.getTimeInMillis(),
					  (Integer) pairs.getValue());
	            ls.add(item);
			it.remove();
		}
        
 

        configuration.addSeries(ls);

        vaadinChartPeriodo.drawChart(configuration);
        Responsive.makeResponsive(vaadinChartPeriodo);
		vaadinChartPeriodo.setSizeFull();
		componentGraficoPeriodo.removeAllComponents();
		componentGraficoPeriodo.setWidth("100%");
		componentGraficoPeriodo.addStyleName("dashboard-panel-slot");

		CssLayout card = new CssLayout();
		card.setWidth("100%");
		card.addStyleName(ValoTheme.LAYOUT_CARD);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("dashboard-panel-toolbar");
		toolbar.setWidth("100%");

		captionInfoPeriodo.addStyleName(ValoTheme.LABEL_H4);
		captionInfoPeriodo.addStyleName(ValoTheme.LABEL_COLORED);
		captionInfoPeriodo.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		vaadinChartPeriodo.setCaption(null);

		MenuBar tools = new MenuBar();
		tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

		MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

			@Override
			public void menuSelected(final MenuItem selectedItem) {
				if (!componentGraficoPeriodo.getStyleName().contains("max")) {
					selectedItem.setIcon(FontAwesome.COMPRESS);
					toggleMaximized(componentGraficoPeriodo, true);
				} else {
					componentGraficoPeriodo.removeStyleName("max");
					selectedItem.setIcon(FontAwesome.EXPAND);
					toggleMaximized(componentGraficoPeriodo, false);
				}
			}
		});
		max.setStyleName("icon-only");

		toolbar.addComponents(captionInfoPeriodo, tools);
		toolbar.setExpandRatio(captionInfoPeriodo, 1);
		toolbar.setComponentAlignment(captionInfoPeriodo, Alignment.MIDDLE_LEFT);

		card.addComponents(toolbar, vaadinChartPeriodo);
		componentGraficoPeriodo.addComponent(card);
	}
		
	private Component createContentWrapperDispositivos(final Component content) {
		final CssLayout slot = new CssLayout();
		slot.setWidth("100%");
		slot.addStyleName("dashboard-panel-slot");

		CssLayout card = new CssLayout();
		card.setWidth("100%");
		card.addStyleName(ValoTheme.LAYOUT_CARD);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("dashboard-panel-toolbar");
		toolbar.setWidth("100%");
		captionTablaDispositivos.addStyleName(ValoTheme.LABEL_H4);
		captionTablaDispositivos.addStyleName(ValoTheme.LABEL_COLORED);
		captionTablaDispositivos.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		content.setCaption(null);

		MenuBar tools = new MenuBar();
		tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

		MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

			@Override
			public void menuSelected(final MenuItem selectedItem) {
				if (!slot.getStyleName().contains("max")) {
					selectedItem.setIcon(FontAwesome.COMPRESS);
					toggleMaximized(slot, true);
				} else {
					slot.removeStyleName("max");
					selectedItem.setIcon(FontAwesome.EXPAND);
					toggleMaximized(slot, false);
				}
			}
		});
		max.setStyleName("icon-only");

		toolbar.addComponents(captionTablaDispositivos, tools);
		toolbar.setExpandRatio(captionTablaDispositivos, 1);
		toolbar.setComponentAlignment(captionTablaDispositivos,
				Alignment.MIDDLE_LEFT);

		card.addComponents(toolbar, content);
		slot.addComponent(card);
		return slot;
	}
	
	private Component createContentWrapperPeriodo(final Component content) {
		final CssLayout slot = new CssLayout();
		slot.setWidth("100%");
		slot.addStyleName("dashboard-panel-slot");

		CssLayout card = new CssLayout();
		card.setWidth("100%");
		card.addStyleName(ValoTheme.LAYOUT_CARD);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("dashboard-panel-toolbar");
		toolbar.setWidth("100%");
		captionTablaPeriodo.addStyleName(ValoTheme.LABEL_H4);
		captionTablaPeriodo.addStyleName(ValoTheme.LABEL_COLORED);
		captionTablaPeriodo.addStyleName(ValoTheme.LABEL_NO_MARGIN);
		content.setCaption(null);

		MenuBar tools = new MenuBar();
		tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

		MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

			@Override
			public void menuSelected(final MenuItem selectedItem) {
				if (!slot.getStyleName().contains("max")) {
					selectedItem.setIcon(FontAwesome.COMPRESS);
					toggleMaximized(slot, true);
				} else {
					slot.removeStyleName("max");
					selectedItem.setIcon(FontAwesome.EXPAND);
					toggleMaximized(slot, false);
				}
			}
		});
		max.setStyleName("icon-only");

		toolbar.addComponents(captionTablaPeriodo, tools);
		toolbar.setExpandRatio(captionTablaPeriodo, 1);
		toolbar.setComponentAlignment(captionTablaPeriodo,
				Alignment.MIDDLE_LEFT);

		card.addComponents(toolbar, content);
		slot.addComponent(card);
		return slot;
	}

	private void toggleMaximized(final Component panel, final boolean maximized) {
		for (Iterator<Component> it = root.iterator(); it.hasNext();) {
			it.next().setVisible(!maximized);
		}
		dashboardPanels.setVisible(true);

		for (Iterator<Component> it = dashboardPanels.iterator(); it.hasNext();) {
			Component c = it.next();
			c.setVisible(!maximized);
		}

		if (maximized) {
			panel.setVisible(true);
			panel.addStyleName("max");
		} else {
			panel.removeStyleName("max");
		}
	}




	
	

	public ValueChangeListener cboxChangeListener = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
			try {
				fillData();
			} catch (InvalidResultSetAccessException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	};
	public ValueChangeListener dfChangeListener = new ValueChangeListener() {

		@Override
		public void valueChange(ValueChangeEvent event) {
			Date fecha = dfFecha.getValue();
			String periodo = Utilidades.DATE_FORMAT_LOCAL.format(fecha);
			String captionGrafico = "Cantidad de Oyentes " + periodo;
			String captionTabla = "Estadísticas " + periodo;
			String captionDispositivos = "Tipos de Conexión " + periodo;
			captionInfoPeriodo.setValue(captionGrafico);
			captionTablaDispositivos.setValue(captionDispositivos);
			captionTablaPeriodo.setValue(captionTabla);
			captionInfoDispositivos.setValue(captionDispositivos);
			fillTablePeriodo(fecha, fecha);
			fillTableDispositivos(fecha, fecha);
			try {
				fillContentWrapperGraficoPeriodo(fecha, fecha);
				fillContentWrapperGraficoDispositivos(fecha, fecha);
			} catch (InvalidResultSetAccessException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	};

	public void fillData() throws InvalidResultSetAccessException,
			ParseException {
		String periodo = (String) cboxPeriodo.getValue();
		String captionGrafico = "Cantidad de Oyentes " + periodo;
		String captionTabla = "Estadísticas " + periodo;

		String captionDispositivos = "Tipos de Conexión " + periodo;
		dfFecha.setVisible(false);
		dfFecha.setDescription(null);

		dfFecha.removeValueChangeListener(dfChangeListener);
		Date fechaFin = today;
		tipoRango = true;
		Calendar fechaInicio = Calendar.getInstance();
		fechaInicio.setTime(fechaFin);
		switch (periodo) {

		case Utilidades.HOY: {
			fechaInicio.setTime(fechaFin);
			tipoRango = false;

			fillContentWrapperGraficoPeriodo(fechaInicio.getTime(), fechaFin);
			fillContentWrapperGraficoDispositivos(fechaInicio.getTime(), fechaFin);
			fillTablePeriodo(fechaInicio.getTime(), fechaFin);
			fillTableDispositivos(fechaInicio.getTime(), fechaFin);
			captionInfoPeriodo.setValue(captionGrafico);
			captionTablaPeriodo.setValue(captionTabla);
			captionTablaDispositivos.setValue(captionDispositivos);
			captionInfoDispositivos.setValue(captionDispositivos);
			break;
		}

		case Utilidades.ULTIMA_SEMANA: {
			fechaInicio.add(Calendar.DATE, -7);

			fillContentWrapperGraficoPeriodo(fechaInicio.getTime(), fechaFin);
			fillContentWrapperGraficoDispositivos(fechaInicio.getTime(), fechaFin);
			fillTablePeriodo(fechaInicio.getTime(), fechaFin);
			fillTableDispositivos(fechaInicio.getTime(), fechaFin);
			captionInfoPeriodo.setValue(captionGrafico);
			captionTablaPeriodo.setValue(captionTabla);
			captionTablaDispositivos.setValue(captionDispositivos);
			captionInfoDispositivos.setValue(captionDispositivos);
			break;
		}
		case Utilidades.ULTIMA_QUINCENA:{

			fechaInicio.add(Calendar.DATE, -14);

			fillContentWrapperGraficoPeriodo(fechaInicio.getTime(), fechaFin);
			fillContentWrapperGraficoDispositivos(fechaInicio.getTime(), fechaFin);
			fillTablePeriodo(fechaInicio.getTime(), fechaFin);
			fillTableDispositivos(fechaInicio.getTime(), fechaFin);
			captionInfoPeriodo.setValue(captionGrafico);
			captionTablaPeriodo.setValue(captionTabla);
			captionInfoDispositivos.setValue(captionDispositivos);
			captionTablaDispositivos.setValue(captionDispositivos);
			break;
		}
		case Utilidades.ULTIMO_MES:{

			fechaInicio.add(Calendar.DATE, -30);
			

			fillContentWrapperGraficoPeriodo(fechaInicio.getTime(), fechaFin);
			fillContentWrapperGraficoDispositivos(fechaInicio.getTime(), fechaFin);
			fillTablePeriodo(fechaInicio.getTime(), fechaFin);
			fillTableDispositivos(fechaInicio.getTime(), fechaFin);
			captionInfoPeriodo.setValue(captionGrafico);
			captionTablaPeriodo.setValue(captionTabla);
			captionInfoDispositivos.setValue(captionDispositivos);
			captionTablaDispositivos.setValue(captionDispositivos);
			break;
		}
		
			case Utilidades.CUSTOM_DATE: {
				tipoRango = false;
				dfFecha.setVisible(true);
				dfFecha.focus();
				dfFecha.setDescription("Seleccionar Fecha");

				dfFecha.addValueChangeListener(dfChangeListener);
				break;
			}

		}

	}

	
	private void fillTablePeriodo(Date fechaInicio, Date fechaFin) {
		tablaInformacionPeriodo.removeAllItems();				
		LinkedHashMap<String, Double> info = clienteService.getInfoRangoFechas(
				emisora, fechaInicio, fechaFin);
		Iterator it = info.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pairs = (Map.Entry) it.next();
			tablaInformacionPeriodo.addItem(pairs);
			System.out.println(pairs.getKey());

			Item row = tablaInformacionPeriodo.getItem(pairs);
			row.getItemProperty("Key").setValue(pairs.getKey());
			row.getItemProperty("Value").setValue(pairs.getValue());

			it.remove();
		}

	}
	
	private void fillTableDispositivos(Date fechaInicio, Date fechaFin){
		tablaDispositivosPeriodo.removeAllItems();				
		LinkedHashMap<String, Integer> info = clienteService.getCantidadDispositivosRangoFechas(emisora, fechaInicio, fechaFin);
		Iterator it = info.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
			tablaDispositivosPeriodo.addItem(pairs);

			Item row = tablaDispositivosPeriodo.getItem(pairs);
			row.getItemProperty("Key").setValue(pairs.getKey());
			row.getItemProperty("Value").setValue(pairs.getValue());

			it.remove();
		}

		
	}
	

		
}
