package com.dashboardwms.components;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.vaadin.addon.JFreeChartWrapper;

import com.dashboardwms.domain.Aplicacion;
import com.dashboardwms.domain.Servidor;
import com.dashboardwms.geoip.Location;
import com.dashboardwms.geoip.LookupService;
import com.dashboardwms.service.ClienteService;
import com.dashboardwms.service.AplicacionService;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.ThemeResource;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class CountryStatisticsPanel extends Panel {
	
	  private CssLayout dashboardPanels;
	    private final VerticalLayout root;
	    private final ComboBox cboxAplicaciones = new ComboBox();
	    private final Table tablaUsuariosConectados = new Table();
	    private final Table tablaMinutosTotales = new Table();
	    private CssLayout componentGraficoUsuarios =  new CssLayout();
	    private CssLayout componentGraficoMinutos = new CssLayout();
	    private ClienteService clienteService;
	    private LookupService cl;
	    
	    
    public CountryStatisticsPanel() {
    	cboxAplicaciones.setImmediate(true);
        addStyleName(ValoTheme.PANEL_BORDERLESS);
        setSizeFull();
        root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(true);
        root.addStyleName("dashboard-view");
        setContent(root);
        Responsive.makeResponsive(root);
        Responsive.makeResponsive(tablaMinutosTotales);
        Responsive.makeResponsive(tablaUsuariosConectados);
        Responsive.makeResponsive(cboxAplicaciones);
        Responsive.makeResponsive(componentGraficoUsuarios);
        Responsive.makeResponsive(componentGraficoMinutos);
        root.addComponent(buildHeader());
     
    	 	  Component content = buildContent();
    	        root.addComponent(content);
    	        root.setExpandRatio(content, 1);
    	     
   
         
    }

    
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setSpacing(true);
        Responsive.makeResponsive(header);
        buildFilter();
        Label title = new Label("Estad�sticas Por Pa�ses");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H1);
        title.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        header.addComponent(title);
        HorizontalLayout tools = new HorizontalLayout(cboxAplicaciones);
        tools.setSpacing(true);
        tools.addStyleName("toolbar");
        header.addComponent(tools);

        return header;
    }

    
    private void buildFilter() {

		cboxAplicaciones.setImmediate(true);
		cboxAplicaciones.setNullSelectionAllowed(false);
		cboxAplicaciones.setInvalidAllowed(false);
		
    	
    }
    
    private Component buildContent() {
        dashboardPanels = new CssLayout();
        dashboardPanels.addStyleName("dashboard-panels");
        Responsive.makeResponsive(dashboardPanels);

        
        dashboardPanels.addComponent(componentGraficoUsuarios);
        dashboardPanels.addComponent(buildComponentTablaTopSesiones());
       dashboardPanels.addComponent(componentGraficoMinutos);
        dashboardPanels.addComponent(buildComponentTablaTopTiempo());

        return dashboardPanels;
    }
    
    
    private Component buildComponentTablaTopSesiones() {
    	buildTableUsuariosConectados();
        Component contentWrapper = createContentWrapper(tablaUsuariosConectados);
      
        contentWrapper.addStyleName("top10-revenue");
        Responsive.makeResponsive(contentWrapper);
        return contentWrapper;
    }

    private Component buildComponentTablaTopTiempo() {
    	buildTableMinutosTotales();
        Component contentWrapper = createContentWrapper(tablaMinutosTotales);
      
        contentWrapper.addStyleName("top10-revenue");
        Responsive.makeResponsive(contentWrapper);
        return contentWrapper;
    }

    
 
    
    private void fillContentWrapperGraficoMinutos(HashSet<Location> listaPaises){
      	PieDataset datasetMinutosEscuchados = dataSetCantidadMinutos(listaPaises);
    	PieDataset datasetConsolidadoMinutos = DatasetUtilities.createConsolidatedPieDataset(datasetMinutosEscuchados, "Otros", 0.02, 4);
    	Component chartCantidadMinutos = wrapperChartPaises(datasetConsolidadoMinutos);
    	Responsive.makeResponsive(chartCantidadMinutos);
    	chartCantidadMinutos.setCaption("Minutos Conectados");
    	chartCantidadMinutos.setSizeFull();
    	componentGraficoMinutos.removeAllComponents();
    	componentGraficoMinutos.setWidth("100%");
    	componentGraficoMinutos.addStyleName("dashboard-panel-slot");

        CssLayout card = new CssLayout();
        card.setWidth("100%");
        card.addStyleName(ValoTheme.LAYOUT_CARD);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("dashboard-panel-toolbar");
        toolbar.setWidth("100%");

        Label caption = new Label(chartCantidadMinutos.getCaption());
        caption.addStyleName(ValoTheme.LABEL_H4);
        caption.addStyleName(ValoTheme.LABEL_COLORED);
        caption.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        chartCantidadMinutos.setCaption(null);

        MenuBar tools = new MenuBar();
        tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
       
        MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

            @Override
            public void menuSelected(final MenuItem selectedItem) {
                if (!componentGraficoMinutos.getStyleName().contains("max")) {
                    selectedItem.setIcon(FontAwesome.COMPRESS);
                    toggleMaximized(componentGraficoMinutos, true);
                } else {
                	componentGraficoMinutos.removeStyleName("max");
                    selectedItem.setIcon(FontAwesome.EXPAND);
                    toggleMaximized(componentGraficoMinutos, false);
                }
            }
        });
        max.setStyleName("icon-only");
   
    

        toolbar.addComponents(caption, tools);
        toolbar.setExpandRatio(caption, 1);
        toolbar.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);

        card.addComponents(toolbar, chartCantidadMinutos);
        componentGraficoMinutos.addComponent(card);
    }
    
    
    
    
    private void fillContentWrapperGraficoUsuarios(HashSet<Location> listaPaises){
    	PieDataset datasetCantidadPais = dataSetNumeroUsuarios(listaPaises);
    	PieDataset datasetConsolidadoPais = DatasetUtilities.createConsolidatedPieDataset(datasetCantidadPais, "Otros", 0.03, 4);
    	   
    	Component chartCantidadUsuarios = wrapperChartPaises(datasetConsolidadoPais);
    	Responsive.makeResponsive(chartCantidadUsuarios);
    	chartCantidadUsuarios.setCaption("Sesiones");
    	chartCantidadUsuarios.setSizeFull();
    	componentGraficoUsuarios.removeAllComponents();
    	componentGraficoUsuarios.setWidth("100%");
    	componentGraficoUsuarios.addStyleName("dashboard-panel-slot");

        CssLayout card = new CssLayout();
        card.setWidth("100%");
        card.addStyleName(ValoTheme.LAYOUT_CARD);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("dashboard-panel-toolbar");
        toolbar.setWidth("100%");

        Label caption = new Label(chartCantidadUsuarios.getCaption());
        caption.addStyleName(ValoTheme.LABEL_H4);
        caption.addStyleName(ValoTheme.LABEL_COLORED);
        caption.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        chartCantidadUsuarios.setCaption(null);

        MenuBar tools = new MenuBar();
        tools.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
       
        MenuItem max = tools.addItem("", FontAwesome.EXPAND, new Command() {

            @Override
            public void menuSelected(final MenuItem selectedItem) {
                if (!componentGraficoUsuarios.getStyleName().contains("max")) {
                    selectedItem.setIcon(FontAwesome.COMPRESS);
                    toggleMaximized(componentGraficoUsuarios, true);
                } else {
                	componentGraficoUsuarios.removeStyleName("max");
                    selectedItem.setIcon(FontAwesome.EXPAND);
                    toggleMaximized(componentGraficoUsuarios, false);
                }
            }
        });
        max.setStyleName("icon-only");
   
    

        toolbar.addComponents(caption, tools);
        toolbar.setExpandRatio(caption, 1);
        toolbar.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);

        card.addComponents(toolbar, chartCantidadUsuarios);
        componentGraficoUsuarios.addComponent(card);
    }
    
    
    
     
   
    private Component createContentWrapper(final Component content) {
        final CssLayout slot = new CssLayout();
        slot.setWidth("100%");
        slot.addStyleName("dashboard-panel-slot");

        CssLayout card = new CssLayout();
        card.setWidth("100%");
        card.addStyleName(ValoTheme.LAYOUT_CARD);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("dashboard-panel-toolbar");
        toolbar.setWidth("100%");

        Label caption = new Label(content.getCaption());
        caption.addStyleName(ValoTheme.LABEL_H4);
        caption.addStyleName(ValoTheme.LABEL_COLORED);
        caption.addStyleName(ValoTheme.LABEL_NO_MARGIN);
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
   
    

        toolbar.addComponents(caption, tools);
        toolbar.setExpandRatio(caption, 1);
        toolbar.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);

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
 
  
    private void buildTableMinutosTotales(){
    	
    	tablaMinutosTotales.setSizeFull();
    	tablaMinutosTotales.setCaption("Minutos Conectados");
    	tablaMinutosTotales.addStyleName(ValoTheme.TABLE_BORDERLESS);
    	tablaMinutosTotales.addStyleName(ValoTheme.TABLE_NO_STRIPES);
    	tablaMinutosTotales.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
    	tablaMinutosTotales.addStyleName(ValoTheme.TABLE_SMALL);
      	tablaMinutosTotales.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
    	tablaMinutosTotales.addContainerProperty("Pa�s", String.class, null);
    	tablaMinutosTotales.addContainerProperty("Tiempo",  BigDecimal.class, null);
    	tablaMinutosTotales.setRowHeaderMode(Table.RowHeaderMode.ICON_ONLY);
    	tablaMinutosTotales.setColumnAlignment("Tiempo", Align.RIGHT);
    	tablaMinutosTotales.setSortContainerPropertyId("Tiempo");
    	tablaMinutosTotales.setSortAscending(false);
    	tablaMinutosTotales.setColumnWidth(null, 40);
    	
    }
      
     private void fillTableMinutosTotales(HashSet<Location> listaPaises){
    	tablaMinutosTotales.removeAllItems();
    	if(listaPaises != null)
        	for (Location pais : listaPaises) {
    			tablaMinutosTotales.addItem(pais);
    			Item row = tablaMinutosTotales.getItem(pais);
        		row.getItemProperty("Pa�s").setValue(pais.countryName);
    			row.getItemProperty("Tiempo").setValue(pais.getminutosConvertidos());
    			tablaMinutosTotales.setItemIcon(pais, new ThemeResource("../flags/" + pais.countryCode.toLowerCase() + ".png"));
    			
    		}

    	 tablaMinutosTotales.sort();
    

    }
    
    
    private void buildTableUsuariosConectados(){
    	tablaUsuariosConectados.setSizeFull();
    	tablaUsuariosConectados.setCaption("Sesiones");
    	tablaUsuariosConectados.addStyleName(ValoTheme.TABLE_BORDERLESS);
    	 tablaUsuariosConectados.addStyleName(ValoTheme.TABLE_NO_STRIPES);
    	 tablaUsuariosConectados.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
    	 tablaUsuariosConectados.addStyleName(ValoTheme.TABLE_SMALL);
    
    	 tablaUsuariosConectados.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
    	
    	
    	 tablaUsuariosConectados.addContainerProperty("Pa�s", String.class, null);
    	 tablaUsuariosConectados.addContainerProperty("Sesiones",  Integer.class, null);
    	 tablaUsuariosConectados.setRowHeaderMode(Table.RowHeaderMode.ICON_ONLY);
  
    	 tablaUsuariosConectados.setColumnWidth(null, 40);
    	 tablaUsuariosConectados.setColumnAlignment("Sesiones", Align.RIGHT);
    	 tablaUsuariosConectados.setSortContainerPropertyId("Sesiones");
    	 tablaUsuariosConectados.setSortAscending(false);
    	
    	
    }
    
    private void fillTablaUsuariosConectados(HashSet<Location> listaPaises){
    	tablaUsuariosConectados.removeAllItems();
    	if(listaPaises != null)
        	for (Location pais : listaPaises) {
        		tablaUsuariosConectados.addItem(pais);
    			Item row = tablaUsuariosConectados.getItem(pais);
        		row.getItemProperty("Pa�s").setValue(pais.countryName);
    			row.getItemProperty("Sesiones").setValue(pais.getCantidadUsuarios());
    			tablaUsuariosConectados.setItemIcon(pais, new ThemeResource("../flags/" + pais.countryCode.toLowerCase() + ".png"));
    			
    		}

    	tablaUsuariosConectados.sort();
    }
    
    
	private static PieDataset dataSetNumeroUsuarios(HashSet<Location> listaPaises) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if(listaPaises!=null)
        for (Location location : listaPaises) {
			dataset.setValue(location.countryName, location.getCantidadUsuarios());
		}

        return dataset;        
    }
	
	
	private static PieDataset dataSetCantidadMinutos(HashSet<Location> listaPaises) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Location location : listaPaises) {
			dataset.setValue(location.countryName, location.getMinutosEscuchados());
			
		}

        return dataset;        
        
    }
	
    private static JFreeChart chartPaises(PieDataset dataset) {
        
        JFreeChart chart = ChartFactory.createPieChart3D(
            null,  // chart title
            dataset,             // data
            false,               // include legend
            true,
            true
        );
       
       
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
     chart.setTextAntiAlias(true);
        chart.setAntiAlias(true); 
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(300);
        plot.setBackgroundPaint(Color.WHITE);
     
        plot.setOutlinePaint(Color.WHITE);
      
             plot.setDarkerSides(true);
       plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setBackgroundAlpha(0.0F); 
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(new Color(70, 70, 70));
        plot.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        plot.setShadowPaint(null);
        plot.setBaseSectionOutlinePaint(Color.BLACK);
    
        plot.setInteriorGap(0);
        plot.setNoDataMessage("No existen datos disponibles");
        plot.setCircular(true);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} - {2}"));
        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{2}")); 
        return chart;
        
    }
    
	   private static JFreeChartWrapper wrapperChartPaises(PieDataset datasetCantidadPais) {
		   
    JFreeChart createchart = chartPaises(datasetCantidadPais);
  
    return new JFreeChartWrapper(createchart);
	   	}
    
	   
	    public void fillComboBox(List<String> listaAplicaciones){
	    	cboxAplicaciones.removeAllItems();
			BeanItemContainer<String> listaAplicacionesContainer = new BeanItemContainer<String>(String.class);
			listaAplicaciones.add(0, "Todas");
			listaAplicacionesContainer.addAll(listaAplicaciones);
			cboxAplicaciones.setContainerDataSource(listaAplicacionesContainer);
			fillData(clienteService.getCantidadClientesPorPais("Todas", cl));
			
			cboxAplicaciones.addValueChangeListener(new ValueChangeListener() {
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					String aplicacionSeleccionada = (String)cboxAplicaciones.getValue();
				
					fillData(clienteService.getCantidadClientesPorPais(aplicacionSeleccionada, cl));
				}		});
			
			cboxAplicaciones.setValue("Todas");
	    }

	    
	    public void fillData(HashSet<Location> listaPaises){
	    	fillTableMinutosTotales(listaPaises);
	    	fillTablaUsuariosConectados(listaPaises);
	    	fillContentWrapperGraficoUsuarios(listaPaises);
	    	fillContentWrapperGraficoMinutos(listaPaises);
	    	
	    }
	  
	    public void setClienteService(ClienteService clienteService){
	    	this.clienteService = clienteService;
	    }
	    
	    public void setLookupService(LookupService cl){
	    	this.cl = cl;
	    }
}