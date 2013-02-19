package com.example.reporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.james.mime4j.field.datetime.DateTime;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.annotations.Theme;

import com.vaadin.client.BrowserInfo;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI; 
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;




import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.datefield.Resolution;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.themes.Reindeer;





@Theme("reportertheme")
@SuppressWarnings("serial")
public class ReporterUI extends UI {
	
	
	
	Window logWindow;
	
	//кнопки 
	private Button buttonList = new Button();
	private Button buttonRefresh = new Button();  
	private Button buttonCache = new Button();
	
	private PopupDateField datefield = new PopupDateField();
	private Table table;
	private final BrowserFrame frame = new BrowserFrame(); 
	private String stringDate;
	private ComboBox localeSelection;
	private HorizontalSplitPanel doclistSplit = new HorizontalSplitPanel();
	private HorizontalSplitPanel historySplit = new HorizontalSplitPanel();
	private TextArea historyArea = new TextArea();
	private Accordion accord;
	private float splitPos;
	private boolean listButtonFlag = false;
	private String basepath = VaadinService.getCurrent()
			.getBaseDirectory().getAbsolutePath();
	
	private static final Resolution[] resolutions = {Resolution.YEAR, Resolution.MONTH, Resolution.DAY};
	private static final String[] resolutionNames = { "Год", "Месяц", "День"};
	final VerticalLayout vlay = new VerticalLayout();
	final HorizontalLayout hlay = new HorizontalLayout();
	private VerticalSplitPanel split = new VerticalSplitPanel();
	private static final Object resolution_PROPERTY_NAME = "name";
	
	private IndexedContainer getResolutionContainer() {
        IndexedContainer resolutionContainer = new IndexedContainer();
        resolutionContainer.addContainerProperty(resolution_PROPERTY_NAME,
                String.class, null);
        for (int i = 0; i < resolutions.length; i++) {
            Item added = resolutionContainer.addItem(resolutions[i]);
            added.getItemProperty(resolution_PROPERTY_NAME).setValue(
                    resolutionNames[i]);
        }
        return resolutionContainer;
    }
	
	
	@Override
	protected void init(VaadinRequest request) {
		
		//---------------настройка календаря
		datefield.setValue(new java.util.Date());
		datefield.setResolution(Resolution.DAY); 
        datefield.setImmediate(true);
        
        localeSelection = new ComboBox();
        localeSelection.setNullSelectionAllowed(false);
        localeSelection.setImmediate(true);
        localeSelection.setContainerDataSource(getResolutionContainer());
        localeSelection.setItemCaptionPropertyId(resolution_PROPERTY_NAME);
        localeSelection.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        localeSelection.setValue(Resolution.DAY);
		
		
		//----------------задаём стили
		
		split.setStyleName("splitpanel");
		final Page page = this.getPage();
		this.getPage().setTitle("SCADAReports");
		 
		
		//------------------ресурсы
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
		final FileResource accordLoadedIcon = new FileResource(new File(basepath +
				"/WEB-INF/icons/loaded.png"));
		final FileResource accordLoadingIcon = new FileResource(new File(basepath +
				"/WEB-INF/icons/loading.png"));
		
		final FileResource pdfFile = new FileResource(new File(basepath +
				"/WEB-INF/docs/book.pdf"));
		
		//-------------настраиваем иконки
		buttonList.setIcon(settingRes);
		buttonRefresh.setIcon(reloadRes);
		//bookButton.setIcon(bookRes);
		buttonCache.setIcon(cacheNoRes);
		
		//-------------добавляем подсказки
		buttonList.setDescription("Список отчётов");
		buttonRefresh.setDescription("Загрузить отчёт"); 
		//bookButton.setDescription("Протокол работы");
		buttonCache.setDescription("Кэширование");
		datefield.setDescription("Календарь");
		
		//-------------добавляем кнопки в панель
		hlay.setSpacing(true);
		hlay.addComponent(buttonList);
		hlay.addComponent(datefield);
		hlay.addComponent(localeSelection);
		hlay.addComponent(buttonRefresh);  
		hlay.addComponent(buttonCache);

		//-------------------установка позиции сплиттера нижней панели
		WebBrowser browser = VaadinSession.getCurrent().getBrowser();
		split.addComponent(vlay);
		split.addComponent(hlay);
		split.setMaxSplitPosition(Page.getCurrent().getBrowserWindowHeight()-33, Unit.PIXELS);
		split.setMinSplitPosition(Page.getCurrent().getBrowserWindowHeight()-33, Unit.PIXELS);    
		split.setLocked(true);   
		
		
		
		
		//--------------Подстраивание под окно браузера
		
		class MyBrowserResizeListener implements RefreshListener {
            private static final long serialVersionUID = -8765221895426102605L;
            
            @Override
            public void refresh(final Refresher source) {
            	page.addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
    		        public void browserWindowResized(BrowserWindowResizeEvent event) {
    		            split.setMinSplitPosition(page.getBrowserWindowHeight()-33, Unit.PIXELS);    
    		            split.setLocked(true); 
    		            frame.setHeight("99.5%");
    		    		frame.setWidth("99.5%"); 
    		        }
            	});     
            }
        }
		Refresher refr = new Refresher();
		refr.addListener(new MyBrowserResizeListener());
		addExtension(refr);
		
		
		//---------------настройка таблицы 
		
		//FilesystemContainer docs = new FilesystemContainer(new File(basepath+"/WEB-INF/docs"));
		List<CReport> listReport = this.getReportList();
		
		//table = new Table(null,docs);
		table = new Table();
		table.addContainerProperty("Имя отчёта", String.class, null);
		table.addContainerProperty("Тип отчёта", CReport.CalendarType.class, null);
		
			
		
		int i = 0;
		while (i<listReport.size()){
			
			Object newItemId = table.addItem();
			Item row1 = table.getItem(newItemId);  
		    row1.getItemProperty("Имя отчёта").setValue(listReport.get(i).m_reportName);
		    row1.getItemProperty("Тип отчёта").setValue(listReport.get(i).m_calendarType); 
		     
		    i++;
		}
		
		table.setSelectable(true); 
		table.setHeight(100f, Unit.PERCENTAGE); 
		table.setWidth(100f, Unit.PERCENTAGE); 			
		
		//----------------добавляем в верхний layout
		accord = new Accordion();
		accord.addTab(table,"Отчёты",accordLoadedIcon);
		accord.addTab(new Label("Изменяемые отчёты"),"Изменяемые отчёты",accordLoadingIcon);
		accord.setHeight("99.7%");
		accord.setWidth("99.7%"); 
		accord.addSelectedTabChangeListener(new Accordion.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				TabSheet tabsheet = event.getTabSheet();
				 
			}
		});
		 
		vlay.setHeight(100f, Unit.PERCENTAGE); 
		frame.setHeight("99.6%");
		frame.setWidth("99.6%"); 
		doclistSplit.addComponent(accord);
		doclistSplit.addComponent(frame);
		doclistSplit.setMaxSplitPosition(30, Unit.PERCENTAGE);  
		doclistSplit.setSplitPosition(30, Unit.PERCENTAGE);
		vlay.addComponent(doclistSplit);   
		
		
		this.setContent(split);
		
		//-----------Слушатели--------------
		
		//комбобокс календаря
		localeSelection.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				datefield.setResolution((Resolution) event.getProperty().getValue());
			}
		});
		
		//таблица
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue()!=null)
				{
					/*File file = (File) event.getProperty().getValue();
					FileResource res = new FileResource(file);
					Date lastModified = new Date(file.lastModified());
					stringDate = new SimpleDateFormat("dd MMM yyyy").format(lastModified);
					frame.setSource(res);*/
					
					CReport.CalendarType calendType;
					System.out.println(event.getProperty().getValue());
					Item row = table.getItem(event.getProperty().getValue());
					System.out.println(row.getItemProperty("Имя отчёта").getValue());  
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
		            		FileResource res = new FileResource(currentFile);
		            		frame.setSource(res);
							flag = true;
		            	}
		            }
		            if (flag == false){
		            	frame.setSource(null);
		            	Notification.show("Отчёт по этой дате не найден");  
		            }
				} 
				else
				{
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
		
		
		//кнопка список отчётов
		
		
		buttonList.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (listButtonFlag == false){
					splitPos = doclistSplit.getSplitPosition();
					doclistSplit.setSplitPosition(0, Unit.PERCENTAGE); 
					listButtonFlag = true;
				}
				else
				{
					if (doclistSplit.getSplitPosition() > 0){
						splitPos = doclistSplit.getSplitPosition();
						doclistSplit.setSplitPosition(0, Unit.PERCENTAGE);					
					} else
					{	
						
						doclistSplit.setSplitPosition(splitPos);
					}
				}
				
			}
		});
		
		
		
	}
	
	
	
	List<CReport> getReportList()
	{
		ArrayList<CReport> res=new ArrayList<CReport>();
		
		CReport r=new CReport();
		r.m_calendarType=CReport.CalendarType.Day;
		r.m_reportID=1;
		r.m_reportName="Отчет 1";
		res.add(r);
 
		r=new CReport();
		r.m_calendarType=CReport.CalendarType.Month;
		r.m_reportID=2;
		r.m_reportName="Отчет 2";
		res.add(r);
		
		
		r=new CReport();
		r.m_calendarType=CReport.CalendarType.Year;
		r.m_reportID=3;
		r.m_reportName="Отчет 3";
		res.add(r);
	
		return res;
	}
	

	String getReportPath(long reportID, DateTime date)
	{
		if  (reportID==1) 
			return basepath +
					"/WEB-INF/docs/1.pdf";
		if  (reportID==2) 
			return basepath +
					"/WEB-INF/docs/2.pdf";
		if  (reportID==3) 
			return basepath +
					"/WEB-INF/docs/3.pdf";
		return "";
	}
	
	
	void cacheUpdateRequest(long reportID, DateTime date)
	{
	}
	
	
	
	
}