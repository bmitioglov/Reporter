package com.example.reporter;

import java.io.File;

import com.vaadin.ui.Alignment;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

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
	private Label stateLabel = new Label("Label");
	
	VerticalLayout vlay = new VerticalLayout();
	HorizontalLayout hlay = new HorizontalLayout();
	
	VerticalSplitPanel split = new VerticalSplitPanel();
	
	
	Panel panel = new Panel();
	
	@Override
	protected void init(VaadinRequest request) {
		
		
		String basepath = VaadinService.getCurrent()
				.getBaseDirectory().getAbsolutePath();
		
		
		this.getPage().setTitle("SCADAReporter");
		
		vlay.addComponent(doclist);
		
		//настраиваем иконки
		
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
		
		buttonList.setIcon(settingRes);
		buttonRefresh.setIcon(reloadRes);
		bookButton.setIcon(bookRes);
		buttonCache.setIcon(cacheNoRes);
		
		
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
		split.setMaxSplitPosition(95, Unit.PERCENTAGE);
		split.setMinSplitPosition(95, Unit.PERCENTAGE);
		
		this.setContent(split);
		
		//Слушатели
		
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
				logWindow = new Window("Протокол работы");
				
				//addWindow(logWindow);
				
				
			}
		});
		
		
		
	}
}