package com.example.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.ui.Alignment;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.data.util.TextFileProperty;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI; 
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.Sizeable.Unit;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.ResizeEvent;
import com.vaadin.ui.themes.Reindeer;

@Theme("reportertheme")
@SuppressWarnings("serial")
public class ReporterUI extends UI {
	
	
	private ComboBox doclist = new ComboBox("Documents");
	
	Window logWindow;
	
	//кнопки 
	private Button buttonList = new Button();
	private Button buttonRefresh = new Button();  
	private Button buttonCache = new Button();
	private Button bookButton = new Button();
	private PopupDateField datefield = new PopupDateField();
	private Label stateLabel = new Label("Отчёт не загружен");
	private Panel panel = new Panel();
	private Table table;
	private Panel docView = new Panel();
	private final BrowserFrame frame = new BrowserFrame();
	private String stringDate;
	
	final VerticalLayout vlay = new VerticalLayout();
	final HorizontalLayout hlay = new HorizontalLayout();
	
	VerticalSplitPanel split = new VerticalSplitPanel();
	
	
	@Override
	protected void init(VaadinRequest request) {
		 
		UI.getCurrent().setStyleName(Reindeer.SPLITPANEL_SMALL); 
		
		final String basepath = VaadinService.getCurrent()
				.getBaseDirectory().getAbsolutePath();
		
		//задаём стили
		
		//split.setStyleName(Reindeer.SPLITPANEL_SMALL);
		split.setStyleName("splitpanel");
		
	
		this.getPage().setTitle("SCADAReporter");
		 
		//vlay.addComponent(docView); 
		
		//ресурсы
		
		FileResource settingRes = new FileResource(new File(basepath +
				"/WEB-INF/icons/settings.png"));
		FileResource reloadRes = new FileResource(new File(basepath +
				"/WEB-INF/icons/reload.png"));
		FileResource bookRes = new FileResource(new File(basepath +
				"/WEB-INF/icons/document-txt.png"));
		final FileResource cacheAcceptRes = new FileResource(new File(basepath +
				"/WEB-INF/icons/tick_16.png"));
		final FileResource cacheNoRes = new FileResource(new File(basepath +
				"/WEB-INF/icons/block_16.png"));
		
		final FileResource pdfFile = new FileResource(new File(basepath +
				"/WEB-INF/docs/book.pdf"));
		
		
		buttonList.setIcon(settingRes);
		buttonRefresh.setIcon(reloadRes);
		bookButton.setIcon(bookRes);
		buttonCache.setIcon(cacheNoRes);
		
		//добавляем подсказки
		buttonList.setDescription("Список отчётов");
		buttonRefresh.setDescription("Загрузить отчёт");
		bookButton.setDescription("Протокол работы");
		buttonCache.setDescription("Кэширование");
		datefield.setDescription("Календарь");
		
		
		//добавляем кнопки в панель
		hlay.setSpacing(true);
		hlay.addComponent(buttonList);
		hlay.addComponent(datefield);
		hlay.addComponent(buttonRefresh);
		hlay.addComponent(buttonCache);
		hlay.addComponent(bookButton);
		hlay.addComponent(stateLabel);
		
		
		split.addComponent(vlay);
		split.addComponent(hlay);
		split.setMaxSplitPosition(94.9f, Unit.PERCENTAGE);
		split.setMinSplitPosition(94.9f, Unit.PERCENTAGE);  
		split.setLocked(true);  
		 
		
		// Создаём подокно для протокола
		final Window subWindow = new Window("Протокол работы");
		Layout content = new VerticalLayout();
		subWindow.setContent(content);        
		subWindow.center();
		subWindow.setId("close");
		subWindow.setResizable(false);
		
		//Создаём подокно для списка отчётов
		final Window listWindow = new Window("Список отчётов");
		Layout contentList = new VerticalLayout();
		listWindow.setContent(contentList);        
		listWindow.setPositionX(0);
		listWindow.setPositionY(0);
		listWindow.setId("close");
		listWindow.setHeight(95, Unit.PERCENTAGE); 
		listWindow.setWidth(20, Unit.PERCENTAGE);
		 
		FilesystemContainer docs = new FilesystemContainer(new File(basepath+"/WEB-INF/docs"));
		
		//настройка таблицы 
		table = new Table(null,docs);
		
		table.setSelectable(true);
		table.setVisibleColumns(new Object[]{"Name", 
        "Last Modified"});
		table.setColumnHeader("Name", "Имя отчёта");
		table.setColumnHeader("Last Modified", "Дата изменения");
		
		listWindow.setHeight(94.5f, Unit.PERCENTAGE); 
		listWindow.setWidth(21, Unit.PERCENTAGE);
		
		//table.setWidth(94.5f,Unit.PERCENTAGE);
		table.setHeight(500,Unit.PIXELS);  
		contentList.addComponent(table);  
		
		
		//Tabsheet для протокола работы
		VerticalLayout l1 = new VerticalLayout();
        l1.setMargin(true);
        l1.addComponent(new Label("There are no previously saved actions."));
        // Tab 2 content
        VerticalLayout l2 = new VerticalLayout();
        l2.setMargin(true);
        l2.addComponent(new Label("There are no saved notes."));

		
		final TabSheet tab = new TabSheet();

        tab.addTab(l1, "Текущий");
        tab.addTab(l2, "С момента загрузки страницы");
        tab.setHeight("500px");
		tab.setWidth("700px"); 

        content.addComponent(tab);
		
		this.setContent(split);
		
		//Вывод PDF

		frame.setSizeFull();
		//frame.setSource(pdfFile);
		
		//Слушатели
		
		//таблица
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue()!=null)
				{
					File file = (File) event.getProperty().getValue();
					
					FileResource res = new FileResource(file);
					Date lastModified = new Date(file.lastModified());
					
					stringDate = new SimpleDateFormat("dd MMM yyyy").format(lastModified);

					
					stateLabel.setValue("Отчёт "+stringDate); 
					frame.setSource(res);
					//vlay.addComponent(frame);
					vlay.removeAllComponents();
					vlay.addComponent(frame);
					vlay.setHeight(99.836f, Unit.PERCENTAGE);
				}
			}
		});
		table.setImmediate(true);
		
		//Кнопка загрузки отчёта
		buttonRefresh.addClickListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) { 
				
				boolean flag = false;
				if (datefield.getValue()!=null){
					Date date = datefield.getValue();
					File currentFile;
					stringDate = new SimpleDateFormat("dd MMM yyyy").format(date);
				
					String list[] = new File(basepath+"/WEB-INF/docs").list();
		            for(int i = 0; i < list.length; i++){
		            	currentFile = new File(basepath +
		            			"/WEB-INF/docs/"+list[i]);
		            	Date lastModified = new Date(currentFile.lastModified());
		            	String stringDate2 = new SimpleDateFormat("dd MMM yyyy").format(lastModified);
		            	
		            	if (stringDate.equals(stringDate2)){
		            		stateLabel.setValue("Отчёт " + stringDate);
		            		FileResource res = new FileResource(currentFile);
		            		frame.setSource(res);
							vlay.removeAllComponents();
							vlay.addComponent(frame);
							vlay.setHeight(99.836f, Unit.PERCENTAGE);
							flag = true;
		            	}
		            }
		            if (flag == false){
		            	vlay.removeAllComponents(); 
		            	stateLabel.setValue("Отчёт по этой дате не найден");
		            	Notification.show("Отчёт по этой дате не найден"); 
		            }
				} 
				else
				{
					stateLabel.setValue("Выберите дату!");
					Notification.show("Выберите дату!");
				}
			}
		});
		
		//кнопка кэша
		buttonCache.addClickListener(new Button.ClickListener(){
			public void buttonClick(ClickEvent event){
				if (buttonCache.getIcon().equals(cacheNoRes)){
					buttonCache.setIcon(cacheAcceptRes);
					
				}
				else
				{
					buttonCache.setIcon(cacheNoRes);
				}
			}
		});
		
		//кнопка протокол работы
		bookButton.addClickListener(new Button.ClickListener(){
			public void buttonClick(ClickEvent event){
				
				if (subWindow.getId().equals("open")){
					subWindow.close();
					subWindow.setId("close");
				} else
				{
			
					UI.getCurrent().addWindow(subWindow);
					subWindow.setId("open");
				}
			}
		});
		
		//кнопка список отчётов
		buttonList.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (listWindow.getId().equals("open")){
					listWindow.close();
					listWindow.setId("close");
				} else
				{
			
					UI.getCurrent().addWindow(listWindow);
					listWindow.setId("open");
				}
			}
		});
		
		
	}
}