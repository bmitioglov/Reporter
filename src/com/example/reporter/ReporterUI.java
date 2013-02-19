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
	
	//������ 
	private Button buttonList = new Button();
	private Button buttonRefresh = new Button();  
	private Button buttonCache = new Button();
	//private Button bookButton = new Button();   
	
	private PopupDateField datefield = new PopupDateField();
	//private Label stateLabel = new Label("����� �� ��������");
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
	
	private static final Resolution[] resolutions = {Resolution.YEAR, Resolution.MONTH, Resolution.DAY};
	private static final String[] resolutionNames = { "���", "�����", "����"};
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
		 
		//UI.getCurrent().setStyleName(Reindeer.SPLITPANEL_SMALL); 
		
		final String basepath = VaadinService.getCurrent()
				.getBaseDirectory().getAbsolutePath();
		
		
		
		//��������� ���������
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
		
		
		//����� �����
		
		split.setStyleName("splitpanel");
		//final Page page = Page.getCurrent();
		final Page page = this.getPage();
		this.getPage().setTitle("SCADAReports");
		 
		
		//�������
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
		
		//����������� ������
		buttonList.setIcon(settingRes);
		buttonRefresh.setIcon(reloadRes);
		//bookButton.setIcon(bookRes);
		buttonCache.setIcon(cacheNoRes);
		
		//��������� ���������
		buttonList.setDescription("������ �������");
		buttonRefresh.setDescription("��������� �����"); 
		//bookButton.setDescription("�������� ������");
		buttonCache.setDescription("�����������");
		datefield.setDescription("���������");
		
		//��������� ������ � ������
		hlay.setSpacing(true);
		hlay.addComponent(buttonList);
		hlay.addComponent(datefield);
		hlay.addComponent(localeSelection);
		hlay.addComponent(buttonRefresh);  
		hlay.addComponent(buttonCache);
		//hlay.addComponent(bookButton);
		//hlay.addComponent(stateLabel);
		
		//��������� ������� ��������� ������ ������
		WebBrowser browser = VaadinSession.getCurrent().getBrowser();
		split.addComponent(vlay);
		split.addComponent(hlay);
		split.setMaxSplitPosition(Page.getCurrent().getBrowserWindowHeight()-33, Unit.PIXELS);
		split.setMinSplitPosition(Page.getCurrent().getBrowserWindowHeight()-33, Unit.PIXELS);    
		split.setLocked(true);   
		
		//��������� ��������� ������
		historyArea.setCaption("�������� ������");
		historyArea.setInputPrompt("�����");
		
		
		
		//������������� ��� ���� ��������
		
		class MyBrowserResizeListener implements RefreshListener {
            private static final long serialVersionUID = -8765221895426102605L;
            
            @Override
            public void refresh(final Refresher source) {
            	page.addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
    		        public void browserWindowResized(BrowserWindowResizeEvent event) {
    		            //stateLabel.setValue("Change! height="+event.getHeight());
    		            //Label label = new Label();
    		            //historyArea.setValue(historyArea.getValue()+"\n\r"+stateLabel.getValue());
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
		
		
		//��������� ������� 
		FilesystemContainer docs = new FilesystemContainer(new File(basepath+"/WEB-INF/docs"));
		table = new Table(null,docs);
				
		table.setSelectable(true);
		table.setVisibleColumns(new Object[]{"Name", 
        "Last Modified"});
		table.setColumnHeader("Name", "��� ������");
		table.setColumnHeader("Last Modified", "���� ���������");
		table.setHeight(100f, Unit.PERCENTAGE); 
		table.setWidth(100f, Unit.PERCENTAGE); 			
		
		//��������� � ������� layout
		accord = new Accordion();
		accord.addTab(table,"������",accordLoadedIcon);
		accord.addTab(new Label("���������� ������"),"���������� ������",accordLoadingIcon);
		accord.setHeight("99.7%");
		accord.setWidth("99.7%"); 
		accord.addSelectedTabChangeListener(new Accordion.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				TabSheet tabsheet = event.getTabSheet();
				 
			}
		});
		
		System.out.println(Page.getCurrent().getBrowserWindowHeight());  
		vlay.setHeight(100f, Unit.PERCENTAGE); 
		frame.setHeight("99.6%");
		frame.setWidth("99.6%"); 
		//doclistSplit.addComponent(table);
		doclistSplit.addComponent(accord);
		doclistSplit.addComponent(frame);
		//historySplit.addComponent(historyArea); 
		//historySplit.addComponent(frame);
		//historySplit.setMaxSplitPosition(30, Unit.PERCENTAGE); 
		//historySplit.setSplitPosition(0, Unit.PERCENTAGE);
		doclistSplit.setMaxSplitPosition(30, Unit.PERCENTAGE);  
		doclistSplit.setSplitPosition(30, Unit.PERCENTAGE);
		vlay.addComponent(doclistSplit);   
		
		
		/*// ������ ������� ��� ���������
		final Window subWindow = new Window("�������� ������");
		Layout content = new VerticalLayout();
		subWindow.setContent(content);        
		subWindow.center(); 
		subWindow.setId("close");
		subWindow.setResizable(false);*/
		
		//������ ������� ��� ������ �������
		/*final Window listWindow = new Window("������ �������");
		Layout contentList = new VerticalLayout();
		listWindow.setContent(contentList);        
		listWindow.setPositionX(0);
		listWindow.setPositionY(0);
		listWindow.setId("close");
		listWindow.setHeight(95, Unit.PERCENTAGE); 
		listWindow.setWidth(20, Unit.PERCENTAGE);*/
		 
		
		
		
		
		
		/*listWindow.setHeight(94.5f, Unit.PERCENTAGE); 
		listWindow.setWidth(21, Unit.PERCENTAGE);
		
		//table.setWidth(94.5f,Unit.PERCENTAGE);
		//table.setHeight(500,Unit.PIXELS);  
		contentList.addComponent(table);*/
		
		//doclistSplit.addComponent(table);
		
		//doclistSplit.setHeight(100, Unit.PERCENTAGE); 
		
		
		//Tabsheet ��� ��������� ������
		VerticalLayout l1 = new VerticalLayout();
        l1.setMargin(true);
        l1.addComponent(new Label("There are no previously saved actions."));
        // Tab 2 content
        VerticalLayout l2 = new VerticalLayout();
        l2.setMargin(true);
        l2.addComponent(new Label("There are no saved notes."));

		
		final TabSheet tab = new TabSheet();

        tab.addTab(l1, "�������");
        tab.addTab(l2, "� ������� �������� ��������");
        tab.setHeight("500px");
		tab.setWidth("700px"); 

        //content.addComponent(tab);
		
		this.setContent(split);
		
		//���������
		
		//��������� ���������
		localeSelection.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				datefield.setResolution((Resolution) event.getProperty().getValue());
			}
		});
		
		//�������
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue()!=null)
				{
					File file = (File) event.getProperty().getValue();
					
					FileResource res = new FileResource(file);
					Date lastModified = new Date(file.lastModified());
					
					stringDate = new SimpleDateFormat("dd MMM yyyy").format(lastModified);

					
					//stateLabel.setValue("����� "+stringDate); 
					frame.setSource(res);
					/*vlay.removeAllComponents();
					vlay.addComponent(frame);
					vlay.setHeight(99.836f, Unit.PERCENTAGE);*/
				}
			}
		}); 
		table.setImmediate(true);
		
		//������ �������� ������
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
		            		//stateLabel.setValue("����� " + stringDate);
		            		FileResource res = new FileResource(currentFile);
		            		frame.setSource(res);
							/*vlay.removeAllComponents();
							vlay.addComponent(frame);
							vlay.setHeight(99.836f, Unit.PERCENTAGE);*/
							flag = true;
		            	}
		            }
		            if (flag == false){
		            	frame.setSource(null);
		            	//stateLabel.setValue("����� �� ���� ���� �� ������");
		            	Notification.show("����� �� ���� ���� �� ������");  
		            }
				} 
				else
				{
					//stateLabel.setValue("�������� ����!");
					Notification.show("�������� ����!");
				}
			}
		});
		
		//������ ����
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
		
		
		/*bookButton.addClickListener(new Button.ClickListener(){
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
		});*/
		
		//������ �������� ������
		/*bookButton.addClickListener(new Button.ClickListener(){
			public void buttonClick(ClickEvent event){
				if (historySplit.getSplitPosition()>15){   
					historySplit.setSplitPosition(0, Unit.PERCENTAGE);					
				} else
				{
					historySplit.setSplitPosition(30, Unit.PERCENTAGE);
				}
			}
		});*/
		
		//������ ������ �������
		
		
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
		r.m_reportName="����� 1";
		res.add(r);
 
		r=new CReport();
		r.m_calendarType=CReport.CalendarType.Month;
		r.m_reportID=2;
		r.m_reportName="����� 2";
		res.add(r);
		
		
		r=new CReport();
		r.m_calendarType=CReport.CalendarType.Year;
		r.m_reportID=3;
		r.m_reportName="����� 3";
		res.add(r);
	
		return res;
	}
	
	
	String getReportPath(long reportID, DateTime date)
	{
		if  (reportID==1) 
			return "1.pdf";
		if  (reportID==2) 
			return "2.pdf";
		if  (reportID==3) 
			return "3.pdf";
		return "";
	}
	
	
	void cacheUpdateRequest(long reportID, DateTime date)
	{
	}
	
	
	
	
}